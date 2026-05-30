package com.lucaspujia.personalregistry.utils

import android.annotation.SuppressLint
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.database.weight.InMemoryWeightsStorage
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import com.lucaspujia.personalregistry.mainActivity.MainActivityViewModel

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

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun viewModelFromFloats(weights: List<Float>): MainActivityViewModel {
    val initialValues: List<Float> = weights
    val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
    return MainActivityViewModel(
        model = MainActivityModel(memoryStorage)
    )
}

val OUTER_PADDING = 16.dp