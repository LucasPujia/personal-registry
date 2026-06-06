package com.lucaspujia.personalregistry.utils

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.ActiveFilters
import com.lucaspujia.personalregistry.mainActivity.MainActivityActions
import com.lucaspujia.personalregistry.mainActivity.TimeRange
import com.lucaspujia.personalregistry.mainActivity.ViewToggles
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.mainActivity.resolveDateLabels
import com.lucaspujia.personalregistry.mainActivity.settings.ImportExportState
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationDay
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import com.lucaspujia.personalregistry.mainActivity.settings.Setting
import com.lucaspujia.personalregistry.mainActivity.settings.SettingOption
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsActions
import com.lucaspujia.personalregistry.mainActivity.settings.ThemeMode
import kotlinx.coroutines.flow.flowOf

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

fun filtersFromFloats(records: List<Float>, goal: Int? = null): ActiveFilters {
    val recordsFromFloats = recordsFromFloats(records)
    return ActiveFilters(
        minViewValue = if (records.isEmpty()) 0 else records.min().toInt() - 2,
        maxViewValue = if (records.isEmpty()) 100 else records.max().toInt() + 2,
        goalValue = goal,
        records = recordsFromFloats,
        dateLabels = resolveDateLabels(recordsFromFloats)
    )
}

fun recordsFromFloats(values: List<Float>): List<RecordItem> {
    return values.mapIndexed { index, f ->
        RecordItem(
            id = (index + 1).toLong(),
            value1 = f.toDouble(),
            dateKey = "2026-10-0${index + 1}"
        )
    }
}

@Composable
fun defaultWeightRegistry() = Registry(id = 1, name = stringResource(R.string.weight), emoji = "Scale", unit1 = MeasureUnit(stringResource(R.string.kilogram), "kg", 1))

@Composable
fun defaultMoneyRegistry() = Registry(id = 2, name = stringResource(R.string.money), emoji = "Money", unit1 = MeasureUnit(stringResource(R.string.currency), "$", 1))

val mockSettingsViewModel = object : SettingsActions {
    override val themeMode = ThemeMode.SYSTEM
    override val notificationFrequency = NotificationFrequency.OFF
    override val notificationDay = NotificationDay.MONDAY
    override val notificationHour = 8
    override val notificationMinute = 0
    override val importExportState = ImportExportState()

    override fun updateSetting(settingOption: SettingOption, value: Setting) {}
    override fun updateNotificationTime(hour: Int, minute: Int) {}
    override fun exportRecords(registryId: Long) = ""
    override fun importRecords(json: String, registryId: Long) {}
    override fun confirmImport(registryId: Long) {}
    override fun dismissImportError() {}
    override fun dismissImportConfirmation() {}
    override fun dismissSuccessMessage() {}
}

@Composable
fun mockMainActivityViewModel(initialValues: List<Float> = listOf(25f, 30f, 35.5f, 32f, 28f, 29f)): MainActivityActions {
    return object : MainActivityActions {
        val defaultWeightRegistry = defaultWeightRegistry()
        override val activeRegistry = defaultWeightRegistry
        override val allRegistries = flowOf(listOf(defaultWeightRegistry))
        override val filters = filtersFromFloats(initialValues)
        override val viewToggles = ViewToggles()
        override val currentTimeRange = TimeRange.MONTH_1
        override var filtersOpened = false
        override var viewTogglesOpened = false
        override var settingsOpened = false
        override var createRegistryOpened = false
        override fun switchRegistry(registry: Registry) {}
        override fun addRecord(value1: Double, value2: Double?, pickerMillis: Long?) {}
        override fun removeRecord(recordItem: RecordItem) {}
        override fun isSelectableDate(utcTimeMillis: Long) = true
        override fun applyFilters(minViewValue: Int?, maxViewValue: Int?, goalValue: Int?, dateRange: Pair<Long, Long>?) = null
        override fun applyViewToggles(showGraph: Boolean, showList: Boolean) {}
        override fun updateTimeRange(range: TimeRange) {}
        override fun createRegistry(registry: Registry) {}
    }
}
val OUTER_PADDING = 16.dp