package com.example.myapplication.database.weight

import com.example.myapplication.utils.nowUTC
import kotlinx.coroutines.runBlocking

interface WeightsStorage {
    fun readWeights(): List<WeightRecord>
    fun writeWeights(weights: List<WeightRecord>)
    fun addWeight(weight: WeightRecord)
    fun deleteWeight(weight: WeightRecord)
}

class RoomWeightsStorage(
    private val dao: WeightRecordDao,
) : WeightsStorage {
    override fun readWeights(): List<WeightRecord> {
        return runBlocking { dao.getAllWeights() }
    }

    override fun writeWeights(weights: List<WeightRecord>) {
        runBlocking { dao.upsertWeights(weights) }
    }

    override fun addWeight(weight: WeightRecord) {
        runBlocking { dao.insertWeight(weight) }
    }

    override fun deleteWeight(weight: WeightRecord) {
        runBlocking { dao.deleteWeight(weight) }
    }
}

class InMemoryWeightsStorage(
    initialWeights: List<WeightRecord> = emptyList(),
) : WeightsStorage {

    private val weights = initialWeights.toMutableList()

    override fun readWeights(): List<WeightRecord> = weights.toList()

    override fun writeWeights(weights: List<WeightRecord>) {
        this.weights.clear()
        this.weights.addAll(weights)
    }

    override fun addWeight(weight: WeightRecord) {
        this.weights.add(weight)
    }

    override fun deleteWeight(weight: WeightRecord) {
        this.weights.remove(weight)
    }

    companion object {
        fun fromFloats(initialWeightsF: List<Float>): InMemoryWeightsStorage {
            return InMemoryWeightsStorage(
                initialWeightsF.mapIndexed { index, it ->
                    WeightRecord(weight = it, createdAt = nowUTC() - (initialWeightsF.size - index))
                }
            )
        }
    }
}
