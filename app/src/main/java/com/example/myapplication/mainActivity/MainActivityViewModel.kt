package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.mainActivity.weightItem.WeightItem
import com.example.myapplication.utils.forDatePicker
import com.example.myapplication.utils.fromDatePicker
import com.example.myapplication.utils.lastMonthRange
import com.example.myapplication.utils.localDateToDateKey
import com.example.myapplication.utils.now
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

enum class TimeRange(val label: String) {
    DAYS_7("7D"), DAYS_15("15D"), MONTH_1("1M"), MONTH_3("3M"), MONTH_6("6M"), YEAR_1("1A")
}

data class ViewToggles(
    val graph: Boolean = true,
    val list: Boolean = true,
)

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {

    private var allWeights: List<WeightItem> = emptyList()
    private var registeredDateKeys: Set<String> = emptySet()
    var filters by mutableStateOf(ActiveFilters()); private set
    var viewToggles by mutableStateOf(ViewToggles()); private set
    var currentTimeRange by mutableStateOf<TimeRange?>(TimeRange.MONTH_1); private set
    var filtersOpened by mutableStateOf(false)
    var viewTogglesOpened by mutableStateOf(false)

    init {
        val initialWeights = model.getWeights()
        syncWeights(initialWeights)
        if (initialWeights.isNotEmpty()) {
            val weightValues = initialWeights.map { it.weight }
            applyFilters(
                minViewValue = weightValues.min().roundToInt() - 2,
                maxViewValue = weightValues.max().roundToInt() + 2,
            )
        }
    }

    /**
     * [pickerMillis] son los milisegundos UTC-midnight provenientes del DatePicker.
     * Se convierten a LocalDate y dateKey justo aquí; no viajan como Long al modelo.
     */
    fun addWeight(weight: Float, pickerMillis: Long?) {
        viewModelScope.launch {
            val date = pickerMillis?.let { fromDatePicker(it) } ?: now()
            val updatedWeights = withContext(Dispatchers.IO) { model.addWeight(weight, date) }
            syncWeights(updatedWeights)
            reapplyFilters()

            if (updatedWeights.size == 1) {
                val firstWeight = updatedWeights.first().weight.roundToInt()
                applyFilters(
                    minViewValue = firstWeight - 2,
                    maxViewValue = firstWeight + 2,
                )
            }
        }
    }

    fun removeWeight(weightItem: WeightItem) {
        viewModelScope.launch {
            val updatedWeights = withContext(Dispatchers.IO) { model.removeWeight(weightItem) }
            syncWeights(updatedWeights)
            reapplyFilters()
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
        val startDate = when (range) {
            TimeRange.DAYS_7 -> endDate.minusDays(7)
            TimeRange.DAYS_15 -> endDate.minusDays(15)
            TimeRange.MONTH_1 -> endDate.minusMonths(1)
            TimeRange.MONTH_3 -> endDate.minusMonths(3)
            TimeRange.MONTH_6 -> endDate.minusMonths(6)
            TimeRange.YEAR_1 -> endDate.minusYears(1)
        }

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

class MainActivityViewModelFactory(
    private val mainActivityModel: MainActivityModel,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(mainActivityModel) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
