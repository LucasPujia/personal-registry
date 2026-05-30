package com.lucaspujia.personalregistry.database.weight

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

import kotlinx.coroutines.flow.Flow

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records ORDER BY createdAt ASC")
    fun getAllWeightsFlow(): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records ORDER BY createdAt ASC")
    suspend fun getAllWeights(): List<WeightRecord>

    @Insert
    suspend fun insertWeight(weightRecord: WeightRecord): Long

    @Upsert
    suspend fun upsertWeights(weightRecords: List<WeightRecord>)

    @Delete
    suspend fun deleteWeight(weightRecord: WeightRecord)

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteWeightById(id: Long)

    @Query("DELETE FROM weight_records")
    suspend fun deleteAllWeights()
}
