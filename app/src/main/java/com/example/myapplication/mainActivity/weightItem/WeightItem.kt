package com.example.myapplication.mainActivity.weightItem

import com.example.myapplication.extensionFunctions.capitalize
import com.example.myapplication.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.example.myapplication.utils.dateKeyToLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeightItem(
    val weight: Double,
    val date: String, // dd/MM
    val dateKey: String, // yyyy-MM-dd
) {
    fun localDate(): LocalDate = dateKeyToLocalDate(dateKey)

    fun formatted(decimalPrecision: Int = WEIGHT_DECIMAL_PRECISION): String {
        return "$date: ${formattedWeight(decimalPrecision)} kg"
    }

    fun formattedWeight(decimalPrecision: Int = WEIGHT_DECIMAL_PRECISION): String {
        return "%.${decimalPrecision}f".format(weight)
    }

    fun getFullDateText(): String {
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM")
        return localDate().format(formatter)
    }

    fun getDayOfWeekText(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE")
        return localDate().format(formatter).capitalize()
    }

    fun getDifferenceString(previousWeight: Double?): String {
        previousWeight ?: return ""
        val diffKg = (weight*1000).toInt() - (previousWeight*1000).toInt()
        if (diffKg == 0) return "="
        return if (diffKg > 0) "+${diffKg}g" else "${diffKg}g"
    }
}