package com.lucaspujia.personalregistry.extensionFunctions

import androidx.compose.material3.DateRangePickerState

/**
 * Retorna el rango seleccionado usando la misma representación que Material DatePicker:
 * midnight UTC del día calendario seleccionado.
 */
fun DateRangePickerState.selectedDateRange(): Pair<Long, Long>? {
    val start = this.selectedStartDateMillis
    val end = this.selectedEndDateMillis
    return if (start != null && end != null) {
        Pair(start, end)
    } else {
        null
    }
}
