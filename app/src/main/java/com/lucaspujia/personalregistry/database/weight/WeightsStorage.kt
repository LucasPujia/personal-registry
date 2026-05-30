package com.lucaspujia.personalregistry.database.weight

import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

interface WeightsStorage {
    fun readWeightsFlow(): Flow<List<WeightRecord>>
    fun readWeights(): List<WeightRecord>
    fun writeWeights(weights: List<WeightRecord>)
    fun addWeight(weight: WeightRecord)
    fun deleteWeight(weight: WeightRecord)
    fun replaceAllWeights(weights: List<WeightRecord>)
}

class RoomWeightsStorage(
    private val dao: WeightRecordDao,
) : WeightsStorage {
    override fun readWeightsFlow(): Flow<List<WeightRecord>> = dao.getAllWeightsFlow()

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

    private val weightsFlow = MutableStateFlow(initialWeights)

    override fun readWeightsFlow(): Flow<List<WeightRecord>> = weightsFlow.asStateFlow()

    override fun readWeights(): List<WeightRecord> = weightsFlow.value

    override fun writeWeights(weights: List<WeightRecord>) {
        weightsFlow.update { current ->
            val updated = current.toMutableList()
            weights.forEach { newRecord ->
                val index = updated.indexOfFirst { it.dateKey == newRecord.dateKey }
                if (index != -1) {
                    updated[index] = newRecord
                } else {
                    updated.add(newRecord)
                }
            }
            updated
        }
    }

    override fun addWeight(weight: WeightRecord) {
        weightsFlow.update { it + weight }
    }

    override fun deleteWeight(weight: WeightRecord) {
        weightsFlow.update { current -> current.filter { it.dateKey != weight.dateKey } }
    }

    override fun replaceAllWeights(weights: List<WeightRecord>) {
        weightsFlow.value = weights
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
