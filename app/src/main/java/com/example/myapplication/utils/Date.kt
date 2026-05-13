package com.example.myapplication.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.SelectableDates
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun now(): Long {
    return LocalDateTime.now()
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun nowUTC(): Long {
    return LocalDateTime.now()
        .atZone(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
}

fun longUTCToLDT(milliseconds: Long): LocalDateTime {
    return Instant
        .ofEpochMilli(milliseconds)
        .atZone(ZoneId.of("UTC"))
        .toLocalDateTime()
}

fun longToLDT(milliseconds: Long): LocalDateTime {
    return Instant
        .ofEpochMilli(milliseconds)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun selectableDatesTilNow(): SelectableDates {
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= nowUTC()
        }
    }
}

fun defaultDatePickerFormatter(): DatePickerFormatter {
    return object : DatePickerFormatter {
        override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String {
            return resolveMonthYearText(monthMillis)
        }

        override fun formatDate(dateMillis: Long?, locale: CalendarLocale, forContentDescription: Boolean): String {
            return resolveDateText(dateMillis)
        }
    }
}

fun resolveDateText(selectedDateMillisUTC: Long?): String {
    return selectedDateMillisUTC?.let {
        val selectedDate = longUTCToLDT(it)
        val today = longUTCToLDT(nowUTC())
        val dateFormat = DateTimeFormatter.ofPattern("dd/MM")

        when (val dateFormatted = dateFormat.format(selectedDate)) {
            dateFormat.format(today) -> "Hoy"
            dateFormat.format(today.minusDays(1)) -> "Ayer"
            else -> dateFormatted
        }
    } ?: "Hoy"
}

fun resolveMonthYearText(selectedDateMillisUTC: Long?): String {
    return selectedDateMillisUTC?.let { dateMillis ->
        val selectedDate = longUTCToLDT(dateMillis)
        val today = longUTCToLDT(nowUTC())
        val dateFormat = DateTimeFormatter.ofPattern("MMMM, yyyy")

        when (val dateFormatted = dateFormat.format(selectedDate)) {
            dateFormat.format(today) -> "Este mes ($dateFormatted)"
            dateFormat.format(today.minusMonths(1)) -> "Mes pasado ($dateFormatted)"
            else -> dateFormatted.capitalize(Locale.getDefault())
        }
    } ?: "Este mes"
}