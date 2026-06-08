package com.lucaspujia.personalregistry

import com.lucaspujia.personalregistry.database.registry.InMemoryRecordsStorage
import com.lucaspujia.personalregistry.database.registry.InMemoryRegistriesStorage
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MainActivityModelTest {

    private val registriesStorage = InMemoryRegistriesStorage()
    private val recordsStorage = InMemoryRecordsStorage()
    private val model = MainActivityModel(registriesStorage, recordsStorage)

    @Test
    fun addRecord_persistsInStorage() = runBlocking {
        val registryId = 1L
        val date = LocalDate.of(2030, 1, 1)
        val value = 70.0

        model.addRecord(registryId, value, null, date)

        val records = recordsStorage.getRecordsByRegistry(registryId)
        assertEquals(1, records.size)
        val record = records.first()
        assertEquals(registryId, record.registryId)
        assertEquals(value, record.value1, 0.0)
        assertNull(record.value2)
        assertEquals("2030-01-01", record.dateKey)
    }

    @Test
    fun removeRecord_removesSpecificRecord() = runBlocking {
        val registryId = 1L
        model.addRecord(registryId, 65.0, null, LocalDate.now())
        val recordItem = model.getRecordsFlow(registryId).first().first()

        model.removeRecord(recordItem)

        val records = recordsStorage.getRecordsByRegistry(registryId)
        assertEquals(0, records.size)
    }

    @Test
    fun calculateVariation_scenarios() {
        val current = RecordItem(value1 = 75.0, dateKey = "2030-01-02")

        // Positive variation
        val prev1 = RecordItem(value1 = 70.0, dateKey = "2030-01-01")
        assertEquals(5.0, current.calculateVariation(prev1)!!, 0.001)

        // Negative variation
        val prev2 = RecordItem(value1 = 80.0, dateKey = "2030-01-01")
        assertEquals(-5.0, current.calculateVariation(prev2)!!, 0.001)

        // No variation
        val prev3 = RecordItem(value1 = 75.0, dateKey = "2030-01-01")
        assertEquals(0.0, current.calculateVariation(prev3)!!, 0.001)

        // Null previous
        assertNull(current.calculateVariation(null))
    }
}
