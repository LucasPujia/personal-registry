package com.example.myapplication.mainActivity

import com.example.myapplication.database.weight.WeightRecord
import com.example.myapplication.database.weight.WeightsStorage

class MainActivityModel(
    private val storage: WeightsStorage,
) {
    fun getWeights(): List<WeightRecord> {
        return storage.readWeights()
    }

    fun addWeight(weight: Float, date: Long): List<WeightRecord> {
        val newRecord = WeightRecord(weight = weight, createdAt = date)
        storage.addWeight(newRecord)
        return getWeights()
    }

    fun removeWeight(index: Int): List<WeightRecord> {
        val currentWeights = getWeights()

        if (index !in currentWeights.indices) {
            return currentWeights
        }

        val weightToRemove = currentWeights[index]
        storage.deleteWeight(weightToRemove)
        return getWeights()
    }
}
