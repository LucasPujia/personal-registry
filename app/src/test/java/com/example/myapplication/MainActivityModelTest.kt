package com.example.myapplication

import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.MainActivityModel
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

        assertEquals(listOf(70f, 71f), model.getWeights().map { it.weight })
    }

    @Test
    fun removeWeight_ignoresInvalidIndex() {
        val model = MainActivityModel(InMemoryWeightsStorage())

        model.addWeight(65f, LocalDate.of(2030, 1, 1))
        model.removeWeight(3)

        assertEquals(listOf(65f), model.getWeights().map { it.weight })
    }

    @Test
    fun addWeight_storesCorrectDateKey() {
        val model = MainActivityModel(InMemoryWeightsStorage())
        val date = LocalDate.of(2030, 6, 15)

        model.addWeight(72f, date)

        assertEquals("2030-06-15", model.getWeights().first().dateKey)
    }
}
