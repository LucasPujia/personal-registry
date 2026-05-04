package com.example.myapplication

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivityViewModel(
    private val model: MainActivityModel,
) : ViewModel() {
    val weightsList = mutableStateListOf<Int>()

    init {
        syncWeights(model.getWeights())
    }

    fun addWeight(weight: Int) {
        syncWeights(model.addWeight(weight))
    }

    fun removeWeight(index: Int) {
        syncWeights(model.removeWeight(index))
    }

    private fun syncWeights(weights: List<Int>) {
        weightsList.clear()
        weightsList.addAll(weights)
    }
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