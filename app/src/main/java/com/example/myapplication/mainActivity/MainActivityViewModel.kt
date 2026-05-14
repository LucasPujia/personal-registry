package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.database.weight.WeightRecord
import com.example.myapplication.utils.nowUTC
import kotlin.math.roundToInt

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {

    private val weightsList = mutableStateListOf<Float>()
    var filters by mutableStateOf(ActiveFilters()); private set
    var filtersOpened by mutableStateOf(false)

    init {
        syncWeights(model.getWeights())
        applyFilters(
            minViewValue = weightsList.min().roundToInt() - 2,
            maxViewValue = weightsList.max().roundToInt() + 2,
        )
        reapplyFilters()
    }

    fun addWeight(weight: Float, date: Long?) {
        syncWeights(model.addWeight(weight, date ?: nowUTC()))
        reapplyFilters()
    }

    fun removeWeight(index: Int) {
        syncWeights(model.removeWeight(index))
        reapplyFilters()
    }

    fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val selectedDay = utcTimeMillis / 86_400_000L
        val currentDay = nowUTC() / 86_400_000L
        val weights = model.getWeights()
        return weights.none { (it.createdAt / 86_400_000L) == selectedDay } && selectedDay <= currentDay
    }

    fun applyFilters(
        minViewValue: Int?,
        maxViewValue: Int?,
        goalWeight: Int? = null,
        dateRange: Pair<Long, Long>? = null,
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
            newWeights.slice(listOf(
                0,
                size / 4,
                size / 2,
                (size * 3) / 4,
                size - 1
            )).map { it.formattedDate() }
        }
    }

    private fun syncWeights(weights: List<WeightRecord>) {
        weightsList.clear()
        weightsList.addAll(weights.map { it.weight })
        filters = filters.copy( weights = weights.map(WeightRecord::toWeightItem) )
    }

    private fun getWeightsFilteredByDate(dateRange: Pair<Long, Long>?): List<WeightRecord> {
        val weights = model.getWeights()
        if (dateRange == null) return weights
        return weights.filter { it.createdAt in dateRange.first..dateRange.second }
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
        get() = weights.map{ it.weight.toFloat() }
    val weightsD: List<Double>
        get() = weights.map{ it.weight }
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