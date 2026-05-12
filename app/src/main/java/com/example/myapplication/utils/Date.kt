package com.example.myapplication.utils

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

fun resolveDateText(selectedDateMillisUTC: Long?): String {
    return selectedDateMillisUTC?.let {
        val selectedDate = longUTCToLDT(it)
        val today = longUTCToLDT(nowUTC())
        val dateFormat = DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault())

        when (dateFormat.format(selectedDate)) {
            dateFormat.format(today) -> "Hoy"
            dateFormat.format(today.minusDays(1)) -> "Ayer"
            else -> dateFormat.format(selectedDate)
        }
    } ?: "Hoy"
}