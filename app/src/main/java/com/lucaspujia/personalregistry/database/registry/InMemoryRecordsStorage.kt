package com.lucaspujia.personalregistry.database.registry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryRecordsStorage(
    initialRecords: List<Record> = emptyList()
) : RecordsStorage {
    private val records = MutableStateFlow(initialRecords)
    private var nextId = (initialRecords.maxOfOrNull { it.id } ?: 0L) + 1

    override fun getRecordsByRegistryFlow(registryId: Long): Flow<List<Record>> =
        records.map { list ->
            list.filter { it.registryId == registryId }
                .sortedWith(compareBy<Record> { it.dateKey }.thenBy { it.createdAt })
        }

    override suspend fun getRecordsByRegistry(registryId: Long): List<Record> =
        records.value.filter { it.registryId == registryId }
            .sortedWith(compareBy<Record> { it.dateKey }.thenBy { it.createdAt })

    override suspend fun insertRecord(record: Record): Long {
        val id = if (record.id == 0L) nextId++ else record.id
        val newRecord = record.copy(id = id)
        records.update { it + newRecord }
        return id
    }

    override suspend fun upsertRecords(records: List<Record>) {
        this.records.update { currentList ->
            val newList = currentList.toMutableList()
            records.forEach { record ->
                val index = newList.indexOfFirst { it.id == record.id && it.id != 0L }
                if (index != -1) {
                    newList[index] = record
                } else {
                    val id = if (record.id == 0L) nextId++ else record.id
                    newList.add(record.copy(id = id))
                }
            }
            newList
        }
    }

    override suspend fun deleteRecord(record: Record) {
        records.update { list ->
            list.filter { it.id != record.id }
        }
    }

    override suspend fun deleteRecordById(id: Long) {
        records.update { list ->
            list.filter { it.id != id }
        }
    }

    override suspend fun deleteAllRecordsByRegistry(registryId: Long) {
        records.update { list ->
            list.filter { it.registryId != registryId }
        }
    }
}
