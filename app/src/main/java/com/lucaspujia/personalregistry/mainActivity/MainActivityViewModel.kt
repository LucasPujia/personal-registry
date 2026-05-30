package com.lucaspujia.personalregistry.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.weight.WeightRecord
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import com.lucaspujia.personalregistry.mainActivity.settings.Setting
import com.lucaspujia.personalregistry.mainActivity.settings.SettingOption
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsRepository
import com.lucaspujia.personalregistry.mainActivity.settings.ThemeMode
import com.lucaspujia.personalregistry.mainActivity.weightItem.WeightItem
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.fromDatePicker
import com.lucaspujia.personalregistry.utils.lastMonthRange
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

data class ImportExportState(
    val showError: Boolean = false,
    val showConfirmation: Boolean = false,
    val successMessageRes: Int? = null,
    val pendingRecords: List<WeightRecord> = emptyList()
)

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val model: MainActivityModel,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private var allWeights: List<WeightItem> = emptyList()
    private var registeredDateKeys: Set<String> = emptySet()

    // Data filters
    var filters by mutableStateOf(ActiveFilters()); private set
    var viewToggles by mutableStateOf(ViewToggles()); private set
    var currentTimeRange by mutableStateOf<TimeRange?>(TimeRange.MONTH_1); private set

    // UI State
    var filtersOpened by mutableStateOf(false)
    var viewTogglesOpened by mutableStateOf(false)
    var settingsOpened by mutableStateOf(false)

    // Settings
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var notificationFrequency by mutableStateOf(NotificationFrequency.OFF)

    // Import/Export state
    var importExportState by mutableStateOf(ImportExportState()); private set

    init {
        settingsRepository.themeModeFlow
            .onEach { themeMode = it }
            .launchIn(viewModelScope)

        settingsRepository.notificationFrequencyFlow
            .onEach { notificationFrequency = it }
            .launchIn(viewModelScope)

        model.weightsFlow
            .onEach { updatedWeights ->
                syncWeights(updatedWeights)

                if (updatedWeights.isNotEmpty() && filters.weights.isEmpty()) {
                    val weightValues = updatedWeights.map { it.weight }
                    applyFilters(
                        minViewValue = weightValues.min().roundToInt() - 2,
                        maxViewValue = weightValues.max().roundToInt() + 2,
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
    fun addWeight(weight: Float, pickerMillis: Long?) {
        viewModelScope.launch {
            val date = pickerMillis?.let { fromDatePicker(it) } ?: now()
            withContext(Dispatchers.IO) { model.addWeight(weight, date) }
        }
    }

    fun removeWeight(weightItem: WeightItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { model.removeWeight(weightItem) }
        }
    }

    /**
     * [utcTimeMillis] proviene del DatePicker (UTC-midnight del día seleccionado).
     * La comparación de unicidad se hace contra el dateKey almacenado, no contra timestamps.
     */
    fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val selectedDate = fromDatePicker(utcTimeMillis)
        val selectedDateKey = localDateToDateKey(selectedDate)

        val hasWeightThisDay = registeredDateKeys.contains(selectedDateKey)

        return !hasWeightThisDay && !selectedDate.isAfter(now())
    }

    fun applyFilters(
        minViewValue: Int? = null,
        maxViewValue: Int? = null,
        goalWeight: Int? = null,
        dateRange: Pair<Long, Long>? = lastMonthRange(),
    ): Int? {
        if (allWeights.isEmpty()) filters = filters.copy(weights = emptyList(), dateLabels = emptyList())
        val newWeights = getWeightsFilteredByDate(dateRange)

        if (newWeights.isEmpty()) return R.string.no_registry_error

        val max = listOfNotNull(
            newWeights.maxOf { it.weight }.toInt() + 2,
            goalWeight?.plus(2),
            maxViewValue
        ).maxOrNull() ?: filters.maxViewValue

        val min = listOfNotNull(
            newWeights.minOf { it.weight }.toInt() - 2,
            goalWeight?.minus(2),
            minViewValue
        ).minOrNull() ?: filters.minViewValue

        filters = filters.copy(
            minViewValue = min,
            maxViewValue = max,
            dateRange = dateRange ?: filters.dateRange,
            goalWeight = goalWeight,
            weights = newWeights,
            shouldAnimate = dateRange != filters.dateRange || goalWeight != filters.goalWeight,
            dateLabels = resolveDateLabels(newWeights),
        )
        return null
    }

    fun applyViewToggles(showGraph: Boolean, showList: Boolean) {
        viewToggles = ViewToggles(graph = showGraph, list = showList)
    }

    fun updateTimeRange(range: TimeRange) {
        currentTimeRange = range
        val endDate = now()
        val startDate = range.apply(endDate)

        applyFilters(
            goalWeight = filters.goalWeight,
            dateRange = Pair(forDatePicker(startDate), forDatePicker(endDate))
        )
    }

    private fun reapplyFilters() {
        applyFilters(
            minViewValue = filters.minViewValue,
            maxViewValue = filters.maxViewValue,
            goalWeight = filters.goalWeight,
            dateRange = filters.dateRange,
        )
    }

    private fun resolveDateLabels(newWeights: List<WeightItem>): List<String> {
        val size = newWeights.size
        return if (size < 6) newWeights.map { it.date } else {
            newWeights.slice(listOf(0, size / 4, size / 2, (size * 3) / 4, size - 1))
                .map { it.date }
        }
    }

    private fun syncWeights(weights: List<WeightItem>) {
        allWeights = weights
        registeredDateKeys = weights.map { it.dateKey }.toSet()
    }

    /**
     * Filtra por rango usando el dateKey de cada registro.
     * Los extremos del rango provienen del DatePicker (UTC-midnight) y se convierten a LocalDate.
     * Así no importa en qué zona horaria fue guardado el registro original.
     */
    private fun getWeightsFilteredByDate(dateRange: Pair<Long, Long>?): List<WeightItem> {
        dateRange ?: return allWeights

        val startDate = fromDatePicker(dateRange.first)
        val endDate = fromDatePicker(dateRange.second)
        return allWeights.filter { it.localDate() in startDate..endDate }
    }
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val weights: List<WeightItem> = emptyList(),
    val dateLabels: List<String> = emptyList(),
    val goalWeight: Int? = null,
    val dateRange: Pair<Long, Long>? = null,
    val shouldAnimate: Boolean = true,
) {
    val weightsF: List<Float> by lazy { weights.map { it.weight.toFloat() } }
    val weightsD: List<Double> by lazy { weights.map { it.weight } }
}
