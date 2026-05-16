package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.database.weight.WeightRecord
import com.example.myapplication.utils.fromDatePicker
import com.example.myapplication.utils.lastMonthRange
import com.example.myapplication.utils.localDateToDateKey
import com.example.myapplication.utils.now
import kotlin.math.roundToInt

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {

    private val weightsList = mutableStateListOf<Float>()
    var filters by mutableStateOf(ActiveFilters()); private set
    var viewToggles by mutableStateOf(ViewToggles()); private set
    var filtersOpened by mutableStateOf(false)
    var viewTogglesOpened by mutableStateOf(false)

    init {
        syncWeights(model.getWeights())
        if (weightsList.isNotEmpty()) {
            applyFilters(
                minViewValue = weightsList.min().roundToInt() - 2,
                maxViewValue = weightsList.max().roundToInt() + 2,
            )
            reapplyFilters()
        }
    }

    /**
     * [pickerMillis] son los milisegundos UTC-midnight provenientes del DatePicker.
     * Se convierten a LocalDate y dateKey justo aquí; no viajan como Long al modelo.
     */
    fun addWeight(weight: Float, pickerMillis: Long?) {
        val date = pickerMillis?.let { fromDatePicker(it) } ?: now()
        syncWeights(model.addWeight(weight, date))
        reapplyFilters()
        if (model.getWeights().size == 1) {
            applyFilters(
                minViewValue = weightsList.min().roundToInt() - 2,
                maxViewValue = weightsList.max().roundToInt() + 2,
            )
        }
    }

    fun removeWeight(index: Int) {
        syncWeights(model.removeWeight(index))
        reapplyFilters()
    }

    /**
     * [utcTimeMillis] proviene del DatePicker (UTC-midnight del día seleccionado).
     * La comparación de unicidad se hace contra el dateKey almacenado, no contra timestamps.
     */
    fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val selectedDate = fromDatePicker(utcTimeMillis)
        val selectedDateKey = localDateToDateKey(selectedDate)

        val hasWeightThisDay = model.getWeights().any { it.dateKey == selectedDateKey }

        return !hasWeightThisDay && !selectedDate.isAfter(now())
    }

    fun applyFilters(
        minViewValue: Int?,
        maxViewValue: Int?,
        goalWeight: Int? = null,
        dateRange: Pair<Long, Long>? = lastMonthRange(),
    ) {
        val newWeights = getWeightsFilteredByDate(dateRange)
        if (newWeights.isEmpty()) return
        filters = filters.copy(
            minViewValue = (minViewValue ?: filters.minViewValue).coerceAtMost((newWeights.minOf { it.weight }).roundToInt() - 2),
            maxViewValue = (maxViewValue ?: filters.maxViewValue).coerceAtLeast((newWeights.maxOf { it.weight }).roundToInt() + 2),
            dateRange = dateRange ?: filters.dateRange,
            goalWeight = goalWeight,
            weights = newWeights.map(WeightRecord::toWeightItem),
            shouldAnimate = dateRange != filters.dateRange || goalWeight != filters.goalWeight,
            dates = resolveDateLabels(newWeights),
        )
    }

    fun applyViewToggles(showGraph: Boolean, showList: Boolean) {
        viewToggles = ViewToggles(graph = showGraph, list = showList)
    }

    private fun reapplyFilters() {
        applyFilters(
            minViewValue = filters.minViewValue,
            maxViewValue = filters.maxViewValue,
            goalWeight = filters.goalWeight,
            dateRange = filters.dateRange,
        )
    }

    private fun resolveDateLabels(newWeights: List<WeightRecord>): List<String> {
        val size = newWeights.size
        return if (size < 6) newWeights.map { it.formattedDate() } else {
            newWeights.slice(listOf(0, size / 4, size / 2, (size * 3) / 4, size - 1))
                .map { it.formattedDate() }
        }
    }

    private fun syncWeights(weights: List<WeightRecord>) {
        weightsList.clear()
        weightsList.addAll(weights.map { it.weight })
        filters = filters.copy(weights = weights.map(WeightRecord::toWeightItem))
    }

    /**
     * Filtra por rango usando el dateKey de cada registro.
     * Los extremos del rango provienen del DatePicker (UTC-midnight) y se convierten a LocalDate.
     * Así no importa en qué zona horaria fue guardado el registro original.
     */
    private fun getWeightsFilteredByDate(dateRange: Pair<Long, Long>?): List<WeightRecord> {
        val weights = model.getWeights()
        dateRange ?: return weights

        val startDate = fromDatePicker(dateRange.first)
        val endDate = fromDatePicker(dateRange.second)
        return weights.filter { it.localDate() in startDate..endDate }
    }
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val weights: List<WeightItem> = emptyList(),
    val dates: List<String> = emptyList(),
    val goalWeight: Int? = null,
    val dateRange: Pair<Long, Long>? = null,
    val shouldAnimate: Boolean = true,
) {
    val weightsF: List<Float>
        get() = weights.map { it.weight.toFloat() }
    val weightsD: List<Double>
        get() = weights.map { it.weight }
}

data class ViewToggles(
    val graph: Boolean = true,
    val list: Boolean = true,
)

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