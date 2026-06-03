package com.lucaspujia.personalregistry.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.fromDatePicker
import com.lucaspujia.personalregistry.utils.lastMonthRange
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

enum class TimeRange(val label: String, val apply: (LocalDate) -> LocalDate) {
    DAYS_7("7D", { it.minusDays(7) }),
    DAYS_15("15D", { it.minusDays(15) }),
    MONTH_1("1M", { it.minusMonths(1) }),
    MONTH_3("3M", { it.minusMonths(3) }),
    MONTH_6("6M", { it.minusMonths(6) }),
    YEAR_1("1A", { it.minusYears(1) })
}

data class ViewToggles(
    val graph: Boolean = true,
    val list: Boolean = true,
)

interface MainActivityActions {
    val activeRegistry: Registry?
    val allRegistries: Flow<List<Registry>>
    val filters: ActiveFilters
    val viewToggles: ViewToggles
    val currentTimeRange: TimeRange?
    var filtersOpened: Boolean
    var viewTogglesOpened: Boolean
    var settingsOpened: Boolean
    var createRegistryOpened: Boolean

    fun switchRegistry(registry: Registry)
    fun addRecord(value1: Double, value2: Double?, pickerMillis: Long?)
    fun removeRecord(recordItem: RecordItem)
    fun isSelectableDate(utcTimeMillis: Long): Boolean
    fun applyFilters(
        minViewValue: Int? = null,
        maxViewValue: Int? = null,
        goalValue: Int? = null,
        dateRange: Pair<Long, Long>? = lastMonthRange(),
    ): Int?
    fun applyViewToggles(showGraph: Boolean, showList: Boolean)
    fun updateTimeRange(range: TimeRange)
    fun createRegistry(registry: Registry)
}

val LocalMainActivityActions = staticCompositionLocalOf<MainActivityActions> {
    error("No MainActivityActions provided")
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val model: MainActivityModel,
) : ViewModel(), MainActivityActions {

    override var activeRegistry by mutableStateOf<Registry?>(null); private set
    override val allRegistries: Flow<List<Registry>> = model.registriesFlow

    private var allRecords: List<RecordItem> = emptyList()
    private var registeredDateKeys: Set<String> = emptySet()
    private var recordsJob: Job? = null

    // Data filters
    override var filters by mutableStateOf(ActiveFilters()); private set
    override var viewToggles by mutableStateOf(ViewToggles()); private set
    override var currentTimeRange by mutableStateOf<TimeRange?>(TimeRange.MONTH_1); private set

    // UI State
    override var filtersOpened by mutableStateOf(false)
    override var viewTogglesOpened by mutableStateOf(false)
    override var settingsOpened by mutableStateOf(false)
    override var createRegistryOpened by mutableStateOf(false)

    init {
        allRegistries
            .onEach { registries ->
                if (activeRegistry == null && registries.isNotEmpty()) {
                    switchRegistry(registries.first())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun switchRegistry(registry: Registry) {
        activeRegistry = registry
        recordsJob?.cancel()
        recordsJob = model.getRecordsFlow(registry.id)
            .onEach { updatedRecords ->
                syncRecords(updatedRecords)

                if (updatedRecords.isNotEmpty() && filters.records.isEmpty()) {
                    val values = updatedRecords.map { it.value1 }
                    applyFilters(
                        minViewValue = values.min().roundToInt() - 2,
                        maxViewValue = values.max().roundToInt() + 2,
                    )
                } else {
                    reapplyFilters()
                }
            }
            .launchIn(viewModelScope)
    }
    /**
     * [pickerMillis] son los milisegundos UTC-midnight provenientes del DatePicker.
     * Se convierten a LocalDate y dateKey justo aquí; no viajan como Long al modelo.
     */
    override fun addRecord(value1: Double, value2: Double?, pickerMillis: Long?) {
        val registryId = activeRegistry?.id ?: return
        viewModelScope.launch {
            val date = pickerMillis?.let { fromDatePicker(it) } ?: now()
            withContext(Dispatchers.IO) { model.addRecord(registryId, value1, value2, date) }
        }
    }

    override fun removeRecord(recordItem: RecordItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { model.removeRecord(recordItem) }
        }
    }

    override fun createRegistry(registry: Registry) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { model.insertRegistry(registry) }
            val newRegistry = registry.copy(id = id)
            switchRegistry(newRegistry)
            createRegistryOpened = false
        }
    }
    /**
     * [utcTimeMillis] proviene del DatePicker (UTC-midnight del día seleccionado).
     * La comparación de unicidad se hace contra el dateKey almacenado, no contra timestamps.
     */
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val selectedDate = fromDatePicker(utcTimeMillis)
        val selectedDateKey = localDateToDateKey(selectedDate)

        val hasRecordThisDay = registeredDateKeys.contains(selectedDateKey)

        return !hasRecordThisDay && !selectedDate.isAfter(now())
    }

    override fun applyFilters(
        minViewValue: Int?,
        maxViewValue: Int?,
        goalValue: Int?,
        dateRange: Pair<Long, Long>?,
    ): Int? {
        if (allRecords.isEmpty()) filters = filters.copy(records = emptyList(), dateLabels = emptyList())
        val newRecords = getRecordsFilteredByDate(dateRange)

        if (newRecords.isEmpty()) return R.string.no_registry_error

        val max = listOfNotNull(
            newRecords.maxOf { it.value1 }.toInt() + 2,
            goalValue?.plus(2),
            maxViewValue
        ).maxOrNull() ?: filters.maxViewValue

        val min = listOfNotNull(
            newRecords.minOf { it.value1 }.toInt() - 2,
            goalValue?.minus(2),
            minViewValue
        ).minOrNull() ?: filters.minViewValue

        filters = ActiveFilters(
            minViewValue = min,
            maxViewValue = max,
            dateRange = dateRange ?: filters.dateRange,
            goalValue = goalValue,
            records = newRecords,
            shouldAnimate = dateRange != filters.dateRange || goalValue != filters.goalValue,
            dateLabels = resolveDateLabels(newRecords),
        )
        return null
    }

    override fun applyViewToggles(showGraph: Boolean, showList: Boolean) {
        viewToggles = ViewToggles(graph = showGraph, list = showList)
    }

    override fun updateTimeRange(range: TimeRange) {
        currentTimeRange = range
        val endDate = now()
        val startDate = range.apply(endDate)

        applyFilters(
            goalValue = filters.goalValue,
            dateRange = Pair(forDatePicker(startDate), forDatePicker(endDate))
        )
    }

    private fun reapplyFilters() {
        applyFilters(
            minViewValue = filters.minViewValue,
            maxViewValue = filters.maxViewValue,
            goalValue = filters.goalValue,
            dateRange = filters.dateRange,
        )
    }

    private fun syncRecords(records: List<RecordItem>) {
        allRecords = records
        registeredDateKeys = records.map { it.dateKey }.toSet()
    }

    /**
     * Filtra por rango usando el dateKey de cada registro.
     * Los extremos del rango provienen del DatePicker (UTC-midnight) y se convierten a LocalDate.
     * Así no importa en qué zona horaria fue guardado el registro original.
     */
    private fun getRecordsFilteredByDate(dateRange: Pair<Long, Long>?): List<RecordItem> {
        dateRange ?: return allRecords

        val startDate = fromDatePicker(dateRange.first)
        val endDate = fromDatePicker(dateRange.second)
        return allRecords.filter { it.localDate() in startDate..endDate }
    }
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val records: List<RecordItem> = emptyList(),
    val dateLabels: List<String> = emptyList(),
    val goalValue: Int? = null,
    val dateRange: Pair<Long, Long>? = null,
    val shouldAnimate: Boolean = true,
) {
    val values1F: List<Float> by lazy { records.map { it.value1.toFloat() } }
    val values1D: List<Double> by lazy { records.map { it.value1 } }
    val values2F: List<Float> by lazy { records.mapNotNull { it.value2?.toFloat() } }
}

fun resolveDateLabels(newRecords: List<RecordItem>): List<String> {
    val size = newRecords.size
    return if (size < 6) newRecords.map { it.date } else {
        newRecords.slice(listOf(0, size / 4, size / 2, (size * 3) / 4, size - 1))
            .map { it.date }
    }
}
