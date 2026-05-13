package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.database.weight.WeightRecord
import com.example.myapplication.utils.nowUTC
import java.time.Instant.ofEpochMilli
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {

    private val weightsList = mutableStateListOf<Float>()
    var filters by mutableStateOf(ActiveFilters()); private set
    var viewMode by mutableStateOf(ViewMode.CHART); private set
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

    // Cíclico
    fun changeViewMode() {
        viewMode = when (viewMode) {
            ViewMode.LIST -> ViewMode.CHART
            ViewMode.CHART -> ViewMode.LIST
        }
    }

    fun reapplyFilters() {
        applyFilters(
            minViewValue = filters.minViewValue,
            maxViewValue = filters.maxViewValue,
            goalWeight = filters.goalWeight,
            dateRange = filters.dateRange,
        )
    }

    fun applyFilters(
        minViewValue: Int?,
        maxViewValue: Int?,
        goalWeight: Int? = null,
        dateRange: Pair<Long, Long>? = null,
    ) {
        val newWeights = getWeightsFilteredByDate(dateRange)
        filters = filters.copy(
            minViewValue = minViewValue ?: filters.minViewValue,
            maxViewValue = maxViewValue ?: filters.maxViewValue,
            dateRange = dateRange ?: filters.dateRange,
            goalWeight = goalWeight,
            weights = newWeights.map { it.weight.toDouble() },
            dates = if (newWeights.size < 10) newWeights.map {
                DateTimeFormatter.ofPattern("MM/dd").format(ofEpochMilli(it.createdAt).atZone(UTC))
            } else listOf(),
            shouldAnimate = dateRange != filters.dateRange || goalWeight != filters.goalWeight,
        )
    }

    private fun syncWeights(weights: List<WeightRecord>) {
        weightsList.clear()
        weightsList.addAll(weights.map { it.weight })
        filters = filters.copy( weights = weightsList.map(Float::toDouble) )
    }

    private fun getWeightsFilteredByDate(dateRange: Pair<Long, Long>?): List<WeightRecord> {
        if (dateRange == null) return model.getWeights()
        return model.getWeights().filter { it.createdAt in dateRange.first..dateRange.second }
    }
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val weights: List<Double> = emptyList(),
    val dates: List<String> = emptyList(),
    val goalWeight: Int? = null,
    val dateRange: Pair<Long, Long>? = null,
    val shouldAnimate: Boolean = true,
) {
    val weightsF: List<Float>
        get() = weights.map(Double::toFloat)
}

enum class ViewMode {
    LIST,
    CHART
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