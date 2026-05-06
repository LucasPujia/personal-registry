package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {

    private val weightsList = mutableStateListOf<Float>()
    var filters by mutableStateOf(ActiveFilters()); private set
    var viewMode by mutableStateOf(ViewMode.LIST); private set

    init {
        syncWeights(model.getWeights())
        applyFilters(Filters(
            minViewValue = weightsList.min() - 2,
            maxViewValue = weightsList.max() + 2,
        ))
        filters = filters.copy(
            weights = weightsList.map(Float::toDouble),
        )
    }

    fun addWeight(weight: Float) {
        syncWeights(model.addWeight(weight))
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

    fun applyFilters(viewFilters: Filters) {
        filters = filters.copy(
            minViewValue = viewFilters.minViewValue?.toDouble() ?: filters.minViewValue,
            maxViewValue = viewFilters.maxViewValue?.toDouble() ?: filters.maxViewValue,
            objectiveWeight = viewFilters.objectiveWeight?.toDouble() ?: filters.objectiveWeight,
//            dateRange = viewFilters.dateRange ?: filters.dateRange,
        )
    }

    private fun syncWeights(weights: List<Float>) {
        weightsList.clear()
        weightsList.addAll(weights)
    }
}

data class ActiveFilters(
    val minViewValue: Double = 0.0,
    val maxViewValue: Double = 100.0,
    val weights: List<Double> = emptyList(),
    val objectiveWeight: Double? = null,
) {
    val weightsF: List<Float>
        get() = weights.map(Double::toFloat)
}

data class Filters(
    val minViewValue: Float? = null,
    val maxViewValue: Float? = null,
    val objectiveWeight: Float? = null,
    val dateRange: Pair<Long, Long>? = null,
)

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