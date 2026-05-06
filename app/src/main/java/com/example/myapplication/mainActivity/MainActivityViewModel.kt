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
    val weightsList = mutableStateListOf<Float>()
    var viewMode by mutableStateOf(ViewMode.LIST)

    init {
        syncWeights(model.getWeights())
    }

    fun addWeight(weight: Float) {
        syncWeights(model.addWeight(weight))
    }

    fun removeWeight(index: Int) {
        syncWeights(model.removeWeight(index))
    }

    fun changeViewMode() {
        viewMode = when (viewMode) {
            ViewMode.LIST -> ViewMode.CHART
            ViewMode.CHART -> ViewMode.LIST
        }
    }

    private fun syncWeights(weights: List<Float>) {
        weightsList.clear()
        weightsList.addAll(weights)
    }
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