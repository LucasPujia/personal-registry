package com.lucaspujia.personalregistry.database.registry

import kotlinx.coroutines.flow.Flow

interface RegistriesStorage {
    fun getAllRegistriesFlow(): Flow<List<Registry>>
    suspend fun getRegistryById(id: Long): Registry?
    suspend fun insertRegistry(registry: Registry): Long
    suspend fun updateRegistry(registry: Registry)
    suspend fun deleteRegistry(registry: Registry)
}

class RoomRegistriesStorage(
    private val registryDao: RegistryDao
) : RegistriesStorage {
    override fun getAllRegistriesFlow(): Flow<List<Registry>> = registryDao.getAllRegistriesFlow()
    override suspend fun getRegistryById(id: Long): Registry? = registryDao.getRegistryById(id)
    override suspend fun insertRegistry(registry: Registry): Long = registryDao.insertRegistry(registry)
    override suspend fun updateRegistry(registry: Registry) = registryDao.updateRegistry(registry)
    override suspend fun deleteRegistry(registry: Registry) = registryDao.deleteRegistry(registry)
}
