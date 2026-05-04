package com.example.myapplication

import kotlinx.coroutines.runBlocking

interface WeightsStorage {
    fun readWeights(): List<Int>
    fun writeWeights(weights: List<Int>)
    suspend fun readWeightsAsync(): List<Int> = readWeights()
    suspend fun writeWeightsAsync(weights: List<Int>) = writeWeights(weights)
}

class RoomWeightsStorage(
    private val dao: WeightRecordDao,
) : WeightsStorage {
    override fun readWeights(): List<Int> {
        return runBlocking {
            dao.getAllWeights().map { it.weight }
        }
    }

    override fun writeWeights(weights: List<Int>) {
        runBlocking {
            dao.deleteAllWeights()
            weights.forEach { weight ->
                dao.insertWeight(WeightRecord(weight = weight))
            }
        }
    }

    override suspend fun readWeightsAsync(): List<Int> {
        return dao.getAllWeights().map { it.weight }
    }

    override suspend fun writeWeightsAsync(weights: List<Int>) {
        dao.deleteAllWeights()
        weights.forEach { weight ->
            dao.insertWeight(WeightRecord(weight = weight))
        }
    }
}

class InMemoryWeightsStorage(
    initialWeights: List<Int> = emptyList(),
) : WeightsStorage {
    private val weights = initialWeights.toMutableList()

    override fun readWeights(): List<Int> = weights.toList()

    override fun writeWeights(weights: List<Int>) {
        this.weights.clear()
        this.weights.addAll(weights)
    }
}
