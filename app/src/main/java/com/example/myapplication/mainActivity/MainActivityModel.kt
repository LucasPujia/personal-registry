package com.example.myapplication.mainActivity

import com.example.myapplication.database.weight.WeightsStorage

class MainActivityModel(
    private val storage: WeightsStorage,
) {
    fun getWeights(): List<Float> {
        return storage.readWeights()
    }

    fun addWeight(weight: Float): List<Float> {
        val updatedWeights = getWeights().toMutableList().apply {
            add(weight)
        }
        storage.writeWeights(updatedWeights)
        return updatedWeights
    }

    fun removeWeight(index: Int): List<Float> {
        val updatedWeights = getWeights().toMutableList()

        if (index !in updatedWeights.indices) {
            return updatedWeights
        }

        updatedWeights.removeAt(index)
        storage.writeWeights(updatedWeights)
        return updatedWeights
    }
}
