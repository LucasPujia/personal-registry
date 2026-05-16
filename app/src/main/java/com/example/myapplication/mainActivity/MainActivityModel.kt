package com.example.myapplication.mainActivity

import com.example.myapplication.database.weight.WeightRecord
import com.example.myapplication.database.weight.WeightsStorage
import com.example.myapplication.utils.forDatePicker
import com.example.myapplication.utils.localDateToDateKey
import java.time.LocalDate

class MainActivityModel(
    private val storage: WeightsStorage,
) {
    fun getWeights(): List<WeightItem> {
        return storage.readWeights().map { it.toWeightItem() }
    }

    fun addWeight(weight: Float, date: LocalDate): List<WeightItem> {
        val newRecord = WeightRecord(
            weight = weight,
            dateKey = localDateToDateKey(date),
            createdAt = forDatePicker(date),
        )
        storage.addWeight(newRecord)
        return getWeights()
    }

    fun removeWeight(weightItem: WeightItem): List<WeightItem> {
        val allRecords = storage.readWeights()
        val recordToRemove = allRecords.find { it.dateKey == weightItem.dateKey }
        if (recordToRemove != null) {
            storage.deleteWeight(recordToRemove)
        }
        return getWeights()
    }
}
