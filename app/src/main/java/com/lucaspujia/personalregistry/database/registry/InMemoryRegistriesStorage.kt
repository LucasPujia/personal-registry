package com.lucaspujia.personalregistry.database.registry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class InMemoryRegistriesStorage(
    initialRegistries: List<Registry> = emptyList()
) : RegistriesStorage {
    private val registries = MutableStateFlow(initialRegistries)
    private var nextId = (initialRegistries.maxOfOrNull { it.id } ?: 0L) + 1

    override fun getAllRegistriesFlow(): Flow<List<Registry>> = registries

    override suspend fun getRegistryById(id: Long): Registry? =
        registries.value.find { it.id == id }

    override suspend fun insertRegistry(registry: Registry): Long {
        val id = if (registry.id == 0L) nextId++ else registry.id
        val newRegistry = registry.copy(id = id)
        registries.update { it + newRegistry }
        return id
    }

    override suspend fun updateRegistry(registry: Registry) {
        registries.update { list ->
            list.map { if (it.id == registry.id) registry else it }
        }
    }

    override suspend fun deleteRegistry(registry: Registry) {
        registries.update { list ->
            list.filter { it.id != registry.id }
        }
    }
}
