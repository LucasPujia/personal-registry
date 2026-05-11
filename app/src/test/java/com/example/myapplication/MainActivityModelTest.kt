package com.example.myapplication

import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.MainActivityModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityModelTest {
    @Test
    fun addWeight_persistsInStorage() {
        val storage = InMemoryWeightsStorage()
        val model = MainActivityModel(storage)

        model.addWeight(70f, 0L)
        model.addWeight(71f, 0L)

        assertEquals(listOf(70f, 71f), model.getWeights().map { it.weight })
    }

    @Test
    fun removeWeight_ignoresInvalidIndex() {
        val model = MainActivityModel(InMemoryWeightsStorage())

        model.addWeight(65f, 0L)
        model.removeWeight(3)

        assertEquals(listOf(65f), model.getWeights().map { it.weight })
    }
}

