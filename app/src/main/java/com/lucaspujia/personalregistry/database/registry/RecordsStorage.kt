package com.lucaspujia.personalregistry.database.registry

import kotlinx.coroutines.flow.Flow

interface RecordsStorage {
    fun getRecordsByRegistryFlow(registryId: Long): Flow<List<Record>>
    suspend fun getRecordsByRegistry(registryId: Long): List<Record>
    suspend fun insertRecord(record: Record): Long
    suspend fun upsertRecords(records: List<Record>)
    suspend fun deleteRecord(record: Record)
    suspend fun deleteRecordById(id: Long)
    suspend fun deleteAllRecordsByRegistry(registryId: Long)
}

class RoomRecordsStorage(
    private val recordDao: RecordDao
) : RecordsStorage {
    override fun getRecordsByRegistryFlow(registryId: Long): Flow<List<Record>> = recordDao.getRecordsByRegistryFlow(registryId)
    override suspend fun getRecordsByRegistry(registryId: Long): List<Record> = recordDao.getRecordsByRegistry(registryId)
    override suspend fun insertRecord(record: Record): Long = recordDao.insertRecord(record)
    override suspend fun upsertRecords(records: List<Record>) = recordDao.upsertRecords(records)
    override suspend fun deleteRecord(record: Record) = recordDao.deleteRecord(record)
    override suspend fun deleteRecordById(id: Long) = recordDao.deleteRecordById(id)
    override suspend fun deleteAllRecordsByRegistry(registryId: Long) = recordDao.deleteAllRecordsByRegistry(registryId)
}
