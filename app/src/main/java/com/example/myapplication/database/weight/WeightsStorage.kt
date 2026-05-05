package com.example.myapplication.database.weight

import kotlinx.coroutines.runBlocking

interface WeightsStorage {
    fun readWeights(): List<Float>
    fun writeWeights(weights: List<Float>)
    suspend fun readWeightsAsync(): List<Float> = readWeights()
    suspend fun writeWeightsAsync(weights: List<Float>) = writeWeights(weights)
}

class RoomWeightsStorage(
    private val dao: WeightRecordDao,
) : WeightsStorage {
    override fun readWeights(): List<Float> {
        return runBlocking {
            dao.getAllWeights().map { it.weight }
        }
    }

    override fun writeWeights(weights: List<Float>) {
        runBlocking {
            dao.deleteAllWeights()
            weights.forEach { weight ->
                dao.insertWeight(WeightRecord(weight = weight))
            }
        }
    }

    override suspend fun readWeightsAsync(): List<Float> {
        return dao.getAllWeights().map { it.weight }
    }

    override suspend fun writeWeightsAsync(weights: List<Float>) {
        dao.deleteAllWeights()
        weights.forEach { weight ->
            dao.insertWeight(WeightRecord(weight = weight))
        }
    }
}

class InMemoryWeightsStorage(
    initialWeights: List<Float> = emptyList(),
) : WeightsStorage {
    private val weights = initialWeights.toMutableList()

    override fun readWeights(): List<Float> = weights.toList()

    override fun writeWeights(weights: List<Float>) {
        this.weights.clear()
        this.weights.addAll(weights)
    }
}
