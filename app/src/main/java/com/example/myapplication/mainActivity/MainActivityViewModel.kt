package com.example.myapplication.mainActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
        applyFilters(Filters(
            minViewValue = weightsList.min().roundToInt() - 2,
            maxViewValue = weightsList.max().roundToInt() + 2,
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
            minViewValue = viewFilters.minViewValue ?: filters.minViewValue,
            maxViewValue = viewFilters.maxViewValue ?: filters.maxViewValue,
            goalWeight = viewFilters.goalWeight,
//            dateRange = viewFilters.dateRange ?: filters.dateRange,
        )
    }

    private fun syncWeights(weights: List<Float>) {
        weightsList.clear()
        weightsList.addAll(weights)
            filters = filters.copy( weights = weightsList.map(Float::toDouble) )
    }
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val weights: List<Double> = emptyList(),
    val goalWeight: Int? = null,
) {
    val weightsF: List<Float>
        get() = weights.map(Double::toFloat)
}

data class Filters(
    val minViewValue: Int? = null,
    val maxViewValue: Int? = null,
    val goalWeight: Int? = null,
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