package com.example.myapplication.mainActivity

import com.example.myapplication.utils.dateKeyToLocalDate
import java.time.LocalDate

data class WeightItem(
    val weight: Double,
    val date: String,
    val dateKey: String,
) {
    fun localDate(): LocalDate = dateKeyToLocalDate(dateKey)

    fun formatted(decimalPrecision: Int = WEIGHT_DECIMAL_PRECISION): String {
        return "$date: ${"%.${decimalPrecision}f".format(weight)} kg"
    }
}
