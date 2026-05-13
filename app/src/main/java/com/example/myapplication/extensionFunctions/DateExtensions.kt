package com.example.myapplication.extensionFunctions

import androidx.compose.material3.DateRangePickerState

fun DateRangePickerState.selectedDateRange(): Pair<Long, Long>? {
    val start = this.selectedStartDateMillis
    val end = this.selectedEndDateMillis
    return if (start != null && end != null) {
        Pair(start, end)
    } else {
        null
    }
}
