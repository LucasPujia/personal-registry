package com.lucaspujia.personalregistry.mainActivity

import com.lucaspujia.personalregistry.database.weight.WeightRecord
import com.lucaspujia.personalregistry.database.weight.WeightsStorage
import com.lucaspujia.personalregistry.mainActivity.weightItem.WeightItem
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.pow
import kotlin.math.round

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

    fun replaceWeights(records: List<WeightRecord>): List<WeightItem> {
        storage.replaceAllWeights(records)
        return getWeights()
    }

    fun getRecordsAsJSON(): String {
        val jsonArray = JSONArray()
        this.storage.readWeights().forEach { record ->
            val jsonObject = JSONObject()
            val factor = 10.0.pow(WEIGHT_DECIMAL_PRECISION)
            val truncatedWeight = round(record.weight * factor) / factor
            jsonObject.put("weight", truncatedWeight)
            jsonObject.put("date", record.dateKey)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString(4)
    }

    fun fromRawJson(json: String): List<WeightRecord>? {
        return try {
            val jsonArray = JSONArray(json)
            val newRecords = mutableListOf<WeightRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val weight = obj.getDouble("weight").toFloat()
                val dateStr = obj.getString("date")
                val date = LocalDate.parse(dateStr)
                newRecords.add(
                    WeightRecord(
                        weight = weight,
                        dateKey = dateStr,
                        createdAt = forDatePicker(date),
                    )
                )
            }
            newRecords
        } catch (_: Exception) {
            null
        }
    }
}
