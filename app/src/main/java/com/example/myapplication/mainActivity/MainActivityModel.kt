package com.example.myapplication.mainActivity

import com.example.myapplication.database.weight.WeightRecord
import com.example.myapplication.database.weight.WeightsStorage
import com.example.myapplication.utils.forDatePicker
import com.example.myapplication.utils.localDateToDateKey
import java.time.LocalDate

class MainActivityModel(
    private val storage: WeightsStorage,
) {
    fun getWeights(): List<WeightRecord> {
        return storage.readWeights()
    }

    fun addWeight(weight: Float, date: LocalDate): List<WeightRecord> {
        val newRecord = WeightRecord(
            weight = weight,
            dateKey = localDateToDateKey(date),
            createdAt = forDatePicker(date),
        )
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
