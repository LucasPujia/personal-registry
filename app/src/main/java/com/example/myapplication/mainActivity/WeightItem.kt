package com.example.myapplication.mainActivity

data class WeightItem(
    val weight: Double,
    val date: String,
) {
    fun formatted(decimalPrecision: Int = WEIGHT_DECIMAL_PRECISION): String {
        return "$date: ${"%.${decimalPrecision}f".format(weight)} kg"
    }
}
