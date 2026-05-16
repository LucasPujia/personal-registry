package com.example.myapplication.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter

fun lastMonthRange() = Pair(forDatePicker(now().minusMonths(1)), todayForDatePicker())

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