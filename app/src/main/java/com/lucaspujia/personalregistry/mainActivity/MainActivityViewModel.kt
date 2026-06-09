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
import javax.inject.Inject

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
    override var registryEditorState by mutableStateOf<RegistryEditorState>(RegistryEditorState.Closed)

    init {
        allRegistries
            .onEach { registries ->
                if (registries.isEmpty()) {
                    registryEditorState = RegistryEditorState.New
                } else if (activeRegistry == null) {
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
                    val values = updatedRecords.map { it.calculatedValue(registry) }
                    applyFilters(
                        minViewValue = values.min() * 0.9,
                        maxViewValue = values.max() * 1.1,
                        goalValue = registry.goalValue,
                        dateRange = filters.dateRange
                    )
                } else {
                    applyFilters(
                        minViewValue = null,
                        maxViewValue = null,
                        goalValue = registry.goalValue,
                        dateRange = filters.dateRange,
                    )
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
            registryEditorState = RegistryEditorState.Closed
        }
    }

    override fun updateRegistry(registry: Registry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { model.updateRegistry(registry) }
            activeRegistry = registry
            registryEditorState = RegistryEditorState.Closed
        }
    }

    override fun deleteRegistry(registry: Registry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { model.deleteRegistry(registry) }
            // Si eliminamos el activo, buscamos otro para mostrar o abrimos creación si no hay más
            if (activeRegistry?.id == registry.id) {
                activeRegistry = null
                registryEditorState = RegistryEditorState.Closed
            }
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
        minViewValue: Double?,
        maxViewValue: Double?,
        goalValue: Double?,
        dateRange: Pair<Long, Long>?,
    ): Int? {
        val registry = activeRegistry ?: return null

        if (goalValue != filters.goalValue) {
            viewModelScope.launch {
                val updatedRegistry = registry.copy(goalValue = goalValue)
                withContext(Dispatchers.IO) { model.updateRegistry(updatedRegistry) }
                activeRegistry = updatedRegistry
            }
        }

        if (allRecords.isEmpty()) filters = filters.copy(records = emptyList(), dateLabels = emptyList())
        val newRecords = getRecordsFilteredByDate(dateRange)

        if (newRecords.isEmpty()) return R.string.no_registry_error

        val calculatedValues = newRecords.map { it.calculatedValue(registry) }

        val min = listOfNotNull(
            calculatedValues.minOrNull()?.times(0.9),
            goalValue?.times(0.9),
            minViewValue
        ).minOrNull() ?: filters.minViewValue

        val max = listOfNotNull(
            calculatedValues.maxOrNull()?.times(1.1),
            goalValue?.times(1.1),
            maxViewValue
        ).maxOrNull() ?: filters.maxViewValue

        filters = ActiveFilters(
            minViewValue = min,
            maxViewValue = max,
            dateRange = dateRange ?: filters.dateRange,
            goalValue = goalValue,
            records = newRecords,
            shouldAnimate = dateRange != filters.dateRange || goalValue != filters.goalValue,
            dateLabels = resolveDateLabels(newRecords),
            calculatedValues = calculatedValues
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

fun resolveDateLabels(newRecords: List<RecordItem>): List<String> {
    val size = newRecords.size
    return if (size < 6) newRecords.map { it.date } else {
        newRecords.slice(listOf(0, size / 4, size / 2, (size * 3) / 4, size - 1))
            .map { it.date }
    }
}
