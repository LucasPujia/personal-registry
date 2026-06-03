package com.lucaspujia.personalregistry.database.registry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistryDao {
    @Query("SELECT * FROM registries ORDER BY id ASC")
    fun getAllRegistriesFlow(): Flow<List<Registry>>

    @Query("SELECT * FROM registries WHERE id = :id")
    suspend fun getRegistryById(id: Long): Registry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistry(registry: Registry): Long

    @Update
    suspend fun updateRegistry(registry: Registry)

    @Delete
    suspend fun deleteRegistry(registry: Registry)
}
