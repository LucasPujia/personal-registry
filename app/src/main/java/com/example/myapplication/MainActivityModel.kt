package com.example.myapplication

class MainActivityModel(
    private val storage: WeightsStorage,
) {
    fun getWeights(): List<Int> {
        return storage.readWeights()
    }

    fun addWeight(weight: Int): List<Int> {
        val updatedWeights = getWeights().toMutableList().apply {
            add(weight)
        }
        storage.writeWeights(updatedWeights)
        return updatedWeights
    }

    fun removeWeight(index: Int): List<Int> {
        val updatedWeights = getWeights().toMutableList()

        if (index !in updatedWeights.indices) {
            return updatedWeights
        }

        updatedWeights.removeAt(index)
        storage.writeWeights(updatedWeights)
        return updatedWeights
    }
}
