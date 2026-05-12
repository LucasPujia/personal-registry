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
    var viewMode by mutableStateOf(ViewMode.CHART); private set
    var filtersOpened by mutableStateOf(false)

    init {
        syncWeights(model.getWeights())
        applyFilters(
            minViewValue = weightsList.min().roundToInt() - 2,
            maxViewValue = weightsList.max().roundToInt() + 2,
        )
        filters = filters.copy(
            weights = weightsList.map(Float::toDouble),
        )
    }

    fun addWeight(weight: Float, date: Long?) {
        syncWeights(model.addWeight(weight, date ?: nowUTC()))
    }

    fun removeWeight(index: Int) {
        syncWeights(model.removeWeight(index))
    }

    // Cíclico
    fun changeViewMode() {
        viewMode = when (viewMode) {
            ViewMode.LIST -> ViewMode.CHART
            ViewMode.CHART -> ViewMode.LIST
        }
    }

    fun applyFilters(
        minViewValue: Int?,
        maxViewValue: Int?,
        goalWeight: Int? = null,
        dateRange: Pair<Long, Long>? = null
    ) {
        filters = filters.copy(
            minViewValue = minViewValue ?: filters.minViewValue,
            maxViewValue = maxViewValue ?: filters.maxViewValue,
            goalWeight = goalWeight,
            weights = dateRange?.let { dr ->
                getWeightsFilteredByDate(dr).map { it.weight.toDouble() }
            } ?: this.weightsList.map(Float::toDouble),
            dateRange = dateRange ?: filters.dateRange,
        )
    }

    private fun syncWeights(weights: List<WeightRecord>) {
        weightsList.clear()
        weightsList.addAll(weights.map { it.weight })
        filters = filters.copy( weights = weightsList.map(Float::toDouble) )
    }

    private fun getWeightsFilteredByDate(dateRange: Pair<Long, Long>): List<WeightRecord> {
        return model.getWeights().filter { it.createdAt in dateRange.first..dateRange.second }
    }
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val weights: List<Double> = emptyList(),
    val goalWeight: Int? = null,
    val dateRange: Pair<Long, Long>? = null,
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