package com.lucaspujia.personalregistry.mainActivity.recordItem

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.extensionFunctions.capitalize
import com.lucaspujia.personalregistry.utils.FormulaEvaluator
import com.lucaspujia.personalregistry.utils.dateKeyToLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Representa un elemento de registro (Record) optimizado para la interfaz de usuario.
 */
data class RecordItem(
    val id: Long = 0,
    val value1: Double,
    val value2: Double? = null,
    val dateKey: String, // Formato: yyyy-MM-dd
) {
    // Retorna la fecha en formato MM/dd para visualización rápida
    val date: String
        get() = dateKey.split("-", limit = 2)[1].replace("-", "/")

    fun formattedDate(): String = localDate().format(DateTimeFormatter.ofPattern("dd/MM"))

    fun localDate(): LocalDate = dateKeyToLocalDate(dateKey)

    /**
     * Retorna el valor de la unidad 1 formateado según la precisión definida.
     */
    fun formattedValue1(registry: Registry): String {
        return "%.${registry.unit1.precision}f".format(value1)
    }

    /**
     * Retorna el valor de la unidad 2 formateado, si existe.
     */
    fun formattedValue2(registry: Registry): String? {
        val u2 = registry.unit2 ?: return null
        val v2 = value2 ?: return null
        return "%.${u2.precision}f".format(v2)
    }

    /**
     * Calcula el valor unificado del registro utilizando la fórmula definida en el Registry.
     * Si no hay fórmula, retorna el valor de la unidad 1.
     */
    fun calculatedValue(registry: Registry): Double {
        return FormulaEvaluator.evaluate(registry.formula, value1, value2)
    }

    /**
     * Calcula la diferencia (variación) entre este registro y el anterior.
     */
    fun calculateVariation(registry: Registry, previous: RecordItem?): Double? {
        if (previous == null) return null
        return calculatedValue(registry) - previous.calculatedValue(registry)
    }

    /**
     * Retorna la variación formateada con signo y unidad.
     */
    fun formattedVariation(registry: Registry, variation: Double?): String? {
        if (variation == null) return null
        if (variation == 0.0) return "="
        val sign = if (variation >= 0) "+" else ""
        return "$sign%.${registry.unit1.precision}f ${registry.unit1.symbol}".format(variation)
    }
}

/**
 * Extensiones de Compose para obtener textos de fecha localizados.
 */
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
