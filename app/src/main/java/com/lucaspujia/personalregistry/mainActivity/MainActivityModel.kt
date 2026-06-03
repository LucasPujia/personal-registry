package com.lucaspujia.personalregistry.mainActivity

import com.lucaspujia.personalregistry.database.registry.Record
import com.lucaspujia.personalregistry.database.registry.RecordsStorage
import com.lucaspujia.personalregistry.database.registry.RegistriesStorage
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MainActivityModel(
    private val registriesStorage: RegistriesStorage,
    private val recordsStorage: RecordsStorage,
) {

    val registriesFlow: Flow<List<Registry>> = registriesStorage.getAllRegistriesFlow()

    fun getRecordsFlow(registryId: Long): Flow<List<RecordItem>> =
        recordsStorage.getRecordsByRegistryFlow(registryId).map { records ->
            records.map { it.toRecordItem() }
        }

    suspend fun getRegistry(id: Long): Registry? = registriesStorage.getRegistryById(id)

    suspend fun insertRegistry(registry: Registry): Long = registriesStorage.insertRegistry(registry)

    suspend fun addRecord(registryId: Long, value1: Double, value2: Double?, date: LocalDate) {
        val newRecord = Record(
            registryId = registryId,
            value1 = value1,
            value2 = value2,
            dateKey = localDateToDateKey(date),
            createdAt = forDatePicker(date),
        )
        recordsStorage.insertRecord(newRecord)
    }

    suspend fun removeRecord(recordItem: RecordItem) {
        recordsStorage.deleteRecordById(recordItem.id)
    }

    // --- Import/Export for Active Registry ---

    fun getRecordsAsJSON(registryId: Long): String {
        val records = kotlinx.coroutines.runBlocking { recordsStorage.getRecordsByRegistry(registryId) }
        val jsonArray = org.json.JSONArray()
        records.forEach { record ->
            val jsonObject = org.json.JSONObject()
            jsonObject.put("value1", record.value1)
            record.value2?.let { jsonObject.put("value2", it) }
            jsonObject.put("date", record.dateKey)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString(4)
    }

    fun fromRawJson(json: String, registryId: Long): List<Record>? {
        return try {
            val jsonArray = org.json.JSONArray(json)
            val newRecords = mutableListOf<Record>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val value1 = obj.getDouble("value1")
                val value2 = if (obj.has("value2")) obj.getDouble("value2") else null
                val dateStr = obj.getString("date")
                val date = LocalDate.parse(dateStr)
                newRecords.add(
                    Record(
                        registryId = registryId,
                        value1 = value1,
                        value2 = value2,
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

    suspend fun replaceRecords(registryId: Long, records: List<Record>) {
        recordsStorage.deleteAllRecordsByRegistry(registryId)
        recordsStorage.upsertRecords(records)
    }
}

fun Record.toRecordItem(): RecordItem = RecordItem(
    id = id,
    value1 = value1,
    value2 = value2,
    dateKey = dateKey
)
