package com.lucaspujia.personalregistry.mainActivity

import com.lucaspujia.personalregistry.database.weight.WeightRecord
import com.lucaspujia.personalregistry.database.weight.WeightsStorage
import com.lucaspujia.personalregistry.mainActivity.weightItem.WeightItem
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
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
