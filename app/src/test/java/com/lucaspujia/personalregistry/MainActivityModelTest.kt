package com.lucaspujia.personalregistry

import com.lucaspujia.personalregistry.database.weight.InMemoryWeightsStorage
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MainActivityModelTest {
    @Test
    fun addWeight_persistsInStorage() {
        val storage = InMemoryWeightsStorage()
        val model = MainActivityModel(storage)

        model.addWeight(70f, LocalDate.of(2030, 1, 1))
        model.addWeight(71f, LocalDate.of(2030, 1, 2))

        assertEquals(listOf(70.0, 71.0), model.getWeights().map { it.weight })
    }

    @Test
    fun removeWeight_removesSpecificRecord() {
        val storage = InMemoryWeightsStorage()
        val model = MainActivityModel(storage)

        model.addWeight(65f, LocalDate.of(2030, 1, 1))
        val records = model.getWeights()
        model.removeWeight(records.first())

        assertEquals(emptyList<Double>(), model.getWeights().map { it.weight })
    }

    @Test
    fun addWeight_storesCorrectDateKey() {
        val model = MainActivityModel(InMemoryWeightsStorage())
        val date = LocalDate.of(2030, 6, 15)

        model.addWeight(72f, date)

        assertEquals("2030-06-15", model.getWeights().first().dateKey)
    }
}
