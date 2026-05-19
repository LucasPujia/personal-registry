package com.example.myapplication.utils

import android.content.Context
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.R
import com.example.myapplication.extensionFunctions.capitalize
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val UTC_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
private val UTC_MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy")

/**
 * Convierte una fecha al UTC-midnight que usa Material3 DatePicker internamente.
 * Solo para inicializar/leer pickers, NO para persistir como identificador de día.
 */
fun forDatePicker(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/**
 * Convierte los milisegundos UTC-midnight del DatePicker de regreso a su fecha calendario.
 */
fun fromDatePicker(utcMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * Serializa una fecha como clave de día estable.
 * Este valor es timezone-independent y nunca se recalcula con la zona actual.
 */
fun localDateToDateKey(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

fun dateKeyToLocalDate(dateKey: String): LocalDate = LocalDate.parse(dateKey)

/**
 * Retorna el UTC-midnight del día actual.
 * Usar SOLO para inicializar/comparar DatePicker, no para persistir.
 */
fun todayForDatePicker(): Long = forDatePicker(now())

fun now(): LocalDate = LocalDate.now()

fun selectableDatesFromFunction(isSelectableDate: (Long) -> Boolean): SelectableDates {
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return isSelectableDate(utcTimeMillis)
        }
    }
}

// --- RESOLVE DATE TEXT ---

@Composable
fun resolveDatePickerText(selectedDateMillisUTC: Long?): String {
    return resolveDatePickerText(LocalContext.current, selectedDateMillisUTC)
}

fun resolveDatePickerText(context: Context?, selectedDateMillisUTC: Long?): String {
    return resolveDateText(context, selectedDateMillisUTC?.let(::fromDatePicker))
}

fun resolveDateText(context: Context?, date: LocalDate?, today: LocalDate = now()): String {
    if (context == null || date == null) return date?.format(UTC_DATE_FORMATTER) ?: ""

    return when (date) {
        today -> context.getString(R.string.today)
        today.minusDays(1) -> context.getString(R.string.yesterday)
        else -> date.format(UTC_DATE_FORMATTER)
    }
}

// --- RESOLVE MONTH YEAR TEXT ---

fun resolveDatePickerMonthYearText(context: Context?, selectedDateMillisUTC: Long?): String {
    return resolveMonthYearText(context, selectedDateMillisUTC?.let(::fromDatePicker))
}

fun resolveMonthYearText(context: Context?, selectedDate: LocalDate?, currentMonth: YearMonth = YearMonth.now()): String {
    if (context == null || selectedDate == null) {
        return selectedDate?.format(UTC_MONTH_FORMATTER)?.replaceFirstChar { it.uppercase() } ?: ""
    }

    val selectedMonth = YearMonth.from(selectedDate)
    val selectedMonthFormated = selectedDate.format(UTC_MONTH_FORMATTER).capitalize()

    return when (selectedMonth) {
        currentMonth -> context.getString(R.string.this_month) + " ($selectedMonthFormated)"
        currentMonth.minusMonths(1) -> context.getString(R.string.last_month) + " ($selectedMonthFormated)"
        else -> selectedMonthFormated
    }
}
