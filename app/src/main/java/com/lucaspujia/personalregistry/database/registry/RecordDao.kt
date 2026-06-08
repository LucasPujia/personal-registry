package com.lucaspujia.personalregistry.database.registry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE registryId = :registryId ORDER BY dateKey ASC, createdAt ASC")
    fun getRecordsByRegistryFlow(registryId: Long): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE registryId = :registryId ORDER BY dateKey ASC, createdAt ASC")
    suspend fun getRecordsByRegistry(registryId: Long): List<Record>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record): Long

    @Upsert
    suspend fun upsertRecords(records: List<Record>)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM records WHERE registryId = :registryId")
    suspend fun deleteAllRecordsByRegistry(registryId: Long)
}
