package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.utils.fromDatePicker
import com.example.myapplication.utils.lastMonthRange
import com.example.myapplication.utils.localDateToDateKey
import com.example.myapplication.utils.now
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {

    private var allWeights: List<WeightItem> = emptyList()
    private var registeredDateKeys: Set<String> = emptySet()
    var filters by mutableStateOf(ActiveFilters()); private set
    var viewToggles by mutableStateOf(ViewToggles()); private set
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
        minViewValue: Int?,
        maxViewValue: Int?,
        goalWeight: Int? = null,
        dateRange: Pair<Long, Long>? = lastMonthRange(),
    ) {
        val newWeights = getWeightsFilteredByDate(dateRange)
        
        val calculatedMin = if (newWeights.isNotEmpty()) (newWeights.minOf { it.weight }).roundToInt() - 2 else filters.minViewValue
        val calculatedMax = if (newWeights.isNotEmpty()) (newWeights.maxOf { it.weight }).roundToInt() + 2 else filters.maxViewValue

        filters = filters.copy(
            minViewValue = minViewValue ?: calculatedMin,
            maxViewValue = maxViewValue ?: calculatedMax,
            dateRange = dateRange ?: filters.dateRange,
            goalWeight = goalWeight,
            weights = newWeights,
            shouldAnimate = dateRange != filters.dateRange || goalWeight != filters.goalWeight,
            dateLabels = resolveDateLabels(newWeights),
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
