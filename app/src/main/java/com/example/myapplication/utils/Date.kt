package com.example.myapplication.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.SelectableDates
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

fun now(): LocalDate = LocalDate.now()

/**
 * Retorna el UTC-midnight del día actual.
 * Usar SOLO para inicializar/comparar DatePicker, no para persistir.
 */
fun todayForDatePicker(): Long = forDatePicker(now())

fun selectableDatesFromFunction(isSelectableDate: (Long) -> Boolean): SelectableDates {
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return isSelectableDate(utcTimeMillis)
        }
    }
}

fun defaultDatePickerFormatter(): DatePickerFormatter {
    return object : DatePickerFormatter {
        override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String {
            return resolveDatePickerMonthYearText(monthMillis)
        }

        override fun formatDate(dateMillis: Long?, locale: CalendarLocale, forContentDescription: Boolean): String {
            return resolveDatePickerText(dateMillis)
        }
    }
}

fun resolveDateText(date: LocalDate?, today: LocalDate = now()): String {
    date ?: return "Hoy"

    return when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> date.format(UTC_DATE_FORMATTER)
    }
}

fun resolveMonthYearText(selectedDate: LocalDate?, currentMonth: YearMonth = YearMonth.now()): String {
    selectedDate ?: return "Este mes"
    val selectedMonth = YearMonth.from(selectedDate)
    val selectedMonthFormated = selectedDate.format(UTC_MONTH_FORMATTER).capitalize()

    return when (selectedMonth) {
        currentMonth -> "Este mes ($selectedMonthFormated)"
        currentMonth.minusMonths(1) -> "Mes pasado ($selectedMonthFormated)"
        else -> selectedMonthFormated
    }
}

fun resolveDatePickerText(selectedDateMillisUTC: Long?): String {
    return resolveDateText(selectedDateMillisUTC?.let(::fromDatePicker))
}

fun resolveDatePickerMonthYearText(selectedDateMillisUTC: Long?): String {
    return resolveMonthYearText(selectedDateMillisUTC?.let(::fromDatePicker))
}