package com.example.myapplication.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.MainActivityModel
import com.example.myapplication.mainActivity.MainActivityViewModel
import com.example.myapplication.mainActivity.settings.SettingsRepository

fun lastMonthRange() = Pair(forDatePicker(now().minusMonths(1)), todayForDatePicker())

@Composable
fun defaultDatePickerFormatter(): DatePickerFormatter {
    val context = LocalContext.current

    return remember(context) {
        object : DatePickerFormatter {
            override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String {
                return resolveDatePickerMonthYearText(context, monthMillis)
            }

            override fun formatDate(dateMillis: Long?, locale: CalendarLocale, forContentDescription: Boolean): String {
                return resolveDatePickerText(context, dateMillis)
            }
        }
    }
}

@Composable
fun viewModelFromFloats(weights: List<Float>): MainActivityViewModel {
    val initialValues: List<Float> = weights
    val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
    val settingsRepository = SettingsRepository(LocalContext.current)
    return MainActivityViewModel(
        model = MainActivityModel(memoryStorage),
        settingsRepository = settingsRepository
    )
}

val OUTER_PADDING = 16.dp