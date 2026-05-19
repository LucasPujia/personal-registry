package com.example.myapplication.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.MainActivityModel
import com.example.myapplication.mainActivity.MainActivityViewModel

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

fun viewModelFromFloats(weights: List<Float>): MainActivityViewModel {
    val initialValues: List<Float> = weights
    val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
    return MainActivityViewModel(MainActivityModel(memoryStorage))
}

val OUTER_PADDING = 16.dp