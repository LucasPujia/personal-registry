package com.lucaspujia.personalregistry.mainActivity.recordItem

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.extensionFunctions.capitalize
import com.lucaspujia.personalregistry.utils.dateKeyToLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class RecordItem(
    val id: Long = 0,
    val value1: Double,
    val value2: Double? = null,
    val dateKey: String, // yyyy-MM-dd
) {
    val date: String
        get() = dateKey.split("-", limit = 2)[1].replace("-","/")

    fun formattedDate(): String = localDate().format(DateTimeFormatter.ofPattern("dd/MM"))

    fun localDate(): LocalDate = dateKeyToLocalDate(dateKey)

    fun formattedValue1(registry: Registry): String {
        return "%.${registry.unit1.precision}f".format(value1)
    }

    fun formattedValue2(registry: Registry): String? {
        val u2 = registry.unit2 ?: return null
        val v2 = value2 ?: return null
        return "%.${u2.precision}f".format(v2)
    }

    fun calculateVariation(previous: RecordItem?): Double? {
        if (previous == null) return null
        return value1 - previous.value1
    }

    fun formattedVariation(registry: Registry, variation: Double?): String? {
        if (variation == null) return null
        if (variation == 0.0) return "="
        val sign = if (variation >= 0) "+" else ""
        return "$sign%.${registry.unit1.precision}f ${registry.unit1.symbol}".format(variation)
    }
}

@Composable
fun RecordItem.getFullDateText(): String {
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
fun RecordItem.getDayOfWeekText(): String {
    val locale = LocalConfiguration.current.locales[0]
    // Formato de día de la semana localizado (ej: "lunes", "Monday")
    val formatter = DateTimeFormatter.ofPattern("EEEE", locale)
    return localDate().format(formatter).capitalize()
}
