package com.lucaspujia.personalregistry.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.mainActivity.ActiveFilters
import com.lucaspujia.personalregistry.mainActivity.MainActivityActions
import com.lucaspujia.personalregistry.mainActivity.TimeRange
import com.lucaspujia.personalregistry.mainActivity.ViewToggles
import com.lucaspujia.personalregistry.mainActivity.resolveDateLabels
import com.lucaspujia.personalregistry.mainActivity.settings.ImportExportState
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationDay
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import com.lucaspujia.personalregistry.mainActivity.settings.Setting
import com.lucaspujia.personalregistry.mainActivity.settings.SettingOption
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsActions
import com.lucaspujia.personalregistry.mainActivity.settings.ThemeMode
import com.lucaspujia.personalregistry.mainActivity.weightItem.WeightItem

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

fun filtersFromFloats(weights: List<Float>, goal: Int? = null): ActiveFilters {
    val weightsFromFloats = weightsFromFloats(weights)
    return ActiveFilters(
        minViewValue = weights.min().toInt() - 2,
        maxViewValue = weights.max().toInt() + 2,
        goalWeight = goal,
        weights = weightsFromFloats,
        dateLabels = resolveDateLabels(weightsFromFloats)
    )
}

fun weightsFromFloats(weights: List<Float>): List<WeightItem> {
    return weights.mapIndexed { index, f -> WeightItem(weight = f.toDouble(), dateKey = "2026-10-0${index + 1}") }
}

val mockSettingsViewModel = object : SettingsActions {
    override val themeMode = ThemeMode.SYSTEM
    override val notificationFrequency = NotificationFrequency.OFF
    override val notificationDay = NotificationDay.MONDAY
    override val notificationHour = 8
    override val notificationMinute = 0
    override val importExportState = ImportExportState()

    override fun updateSetting(settingOption: SettingOption, value: Setting) {}
    override fun updateNotificationTime(hour: Int, minute: Int) {}
    override fun exportWeights() = ""
    override fun importWeights(json: String) {}
    override fun confirmImport() {}
    override fun dismissImportError() {}
    override fun dismissImportConfirmation() {}
    override fun dismissSuccessMessage() {}
}

fun mockMainActivityViewModel(initialValues: List<Float> = listOf(25f, 30f, 35.5f, 32f, 28f, 29f)): MainActivityActions {
    return object : MainActivityActions {
        override val filters = filtersFromFloats(initialValues)
        override val viewToggles = ViewToggles()
        override val currentTimeRange = TimeRange.MONTH_1
        override var filtersOpened = false
        override var viewTogglesOpened = false
        override var settingsOpened = false
        override fun addWeight(weight: Float, pickerMillis: Long?) {}
        override fun removeWeight(weightItem: WeightItem) {}
        override fun isSelectableDate(utcTimeMillis: Long) = true
        override fun applyFilters(minViewValue: Int?, maxViewValue: Int?, goalWeight: Int?, dateRange: Pair<Long, Long>?) = null
        override fun applyViewToggles(showGraph: Boolean, showList: Boolean) {}
        override fun updateTimeRange(range: TimeRange) {}
    }
}
val OUTER_PADDING = 16.dp