package com.lucaspujia.personalregistry.database.weight

import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import kotlinx.coroutines.runBlocking

interface WeightsStorage {
    fun readWeights(): List<WeightRecord>
    fun writeWeights(weights: List<WeightRecord>)
    fun addWeight(weight: WeightRecord)
    fun deleteWeight(weight: WeightRecord)
    fun replaceAllWeights(weights: List<WeightRecord>)
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

    override fun replaceAllWeights(weights: List<WeightRecord>) {
        runBlocking {
            dao.deleteAllWeights()
            dao.upsertWeights(weights)
        }
    }
}

class InMemoryWeightsStorage(
    initialWeights: List<WeightRecord> = emptyList(),
) : WeightsStorage {

    private val weights = initialWeights.toMutableList()

    override fun readWeights(): List<WeightRecord> = weights.toList()

    override fun writeWeights(weights: List<WeightRecord>) {
        weights.forEach { newRecord ->
            val index = this.weights.indexOfFirst { it.dateKey == newRecord.dateKey }
            if (index != -1) {
                this.weights[index] = newRecord
            } else {
                this.weights.add(newRecord)
            }
        }
    }

    override fun addWeight(weight: WeightRecord) {
        this.weights.add(weight)
    }

    override fun deleteWeight(weight: WeightRecord) {
        this.weights.remove(weight)
    }

    override fun replaceAllWeights(weights: List<WeightRecord>) {
        this.weights.clear()
        this.weights.addAll(weights)
    }

    companion object {
        fun fromFloats(initialWeightsF: List<Float>): InMemoryWeightsStorage {
            return InMemoryWeightsStorage(
                initialWeightsF.mapIndexed { index, weight ->
                    val date = now().minusDays((initialWeightsF.size - index - 1).toLong())
                    WeightRecord(
                        weight = weight,
                        dateKey = localDateToDateKey(date),
                        createdAt = forDatePicker(date),
                    )
                }
            )
        }
    }
}
