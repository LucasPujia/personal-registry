package com.lucaspujia.personalregistry.mainActivity.weightItem

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.extensionFunctions.capitalize
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.lucaspujia.personalregistry.utils.dateKeyToLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeightItem(
    val weight: Double,
    val date: String, // dd/MM
    val dateKey: String, // yyyy-MM-dd
) {
    fun localDate(): LocalDate = dateKeyToLocalDate(dateKey)

    fun formattedWeight(decimalPrecision: Int = WEIGHT_DECIMAL_PRECISION): String {
        return "%.${decimalPrecision}f".format(weight)
    }

    fun getDifferenceString(previousWeight: Double?): String {
        previousWeight ?: return ""
        val diffKg = (weight*1000).toInt() - (previousWeight*1000).toInt()
        if (diffKg == 0) return "="
        return if (diffKg > 0) "+${diffKg}g" else "${diffKg}g"
    }
}

@Composable
fun WeightItem.getFullDateText(): String {
    val date = localDate()
    val now = LocalDate.now()
    val locale = LocalConfiguration.current.locales[0]

    return when (date) {
        now -> stringResource(R.string.today)
        now.minusDays(1) -> stringResource(R.string.yesterday)
        else -> {
            // Obtiene el mejor patrón para el idioma del usuario (ej: "15 de julio" o "July 15") sin el año
            val pattern = DateFormat.getBestDateTimePattern(locale, "dMMMM")
            val formatter = DateTimeFormatter.ofPattern(pattern, locale)
            date.format(formatter)
        }
    }
}

@Composable
fun WeightItem.getDayOfWeekText(): String {
    val locale = LocalConfiguration.current.locales[0]
    // Formato de día de la semana localizado (ej: "lunes", "Monday")
    val formatter = DateTimeFormatter.ofPattern("EEEE", locale)
    return localDate().format(formatter).capitalize()
}
