package com.example.myapplication

import com.example.myapplication.mainActivity.MainActivityModel
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityModelTest {
    @Test
    fun addWeight_persistsInStorage() {
        val storage = InMemoryWeightsStorage()
        val model = MainActivityModel(storage)

        model.addWeight(70)
        model.addWeight(71)

        assertEquals(listOf(70, 71), model.getWeights())
    }

    @Test
    fun removeWeight_ignoresInvalidIndex() {
        val model = MainActivityModel(InMemoryWeightsStorage())

        model.addWeight(65)
        model.removeWeight(3)

        assertEquals(listOf(65), model.getWeights())
    }
}

