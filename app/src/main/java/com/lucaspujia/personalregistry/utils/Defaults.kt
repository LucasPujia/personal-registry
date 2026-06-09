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
import com.lucaspujia.personalregistry.mainActivity.RegistryEditorState
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
import kotlin.math.max
import kotlin.math.min

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

fun filtersFromDoubles(records: List<Double>, goal: Double? = null): ActiveFilters {
    val recordItems = recordsFromDoubles(records)
    val minViewValue = min(if (records.isEmpty()) 0.0 else records.min(), goal ?: Double.MAX_VALUE) * 0.9
    val maxViewValue = max(if (records.isEmpty()) 100.0 else records.max(), goal ?: Double.MIN_VALUE) * 1.1
    return ActiveFilters(
        minViewValue = minViewValue,
        maxViewValue = maxViewValue,
        goalValue = goal,
        records = recordItems,
        dateLabels = resolveDateLabels(recordItems),
        calculatedValues = records
    )
}

fun recordsFromDoubles(values: List<Double>): List<RecordItem> {
    return values.mapIndexed { index, v ->
        RecordItem(
            id = (index + 1).toLong(),
            value1 = v,
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
fun mockMainActivityViewModel(initialValues: List<Double> = listOf(25.0, 30.0, 35.5, 32.0, 28.0, 29.0)): MainActivityActions {
    return object : MainActivityActions {
        val defaultWeightRegistry = defaultWeightRegistry()
        override val activeRegistry = defaultWeightRegistry
        override val allRegistries = flowOf(listOf(defaultWeightRegistry))
        override val filters = filtersFromDoubles(initialValues)
        override val viewToggles = ViewToggles()
        override val currentTimeRange = TimeRange.MONTH_1
        override var filtersOpened = false
        override var viewTogglesOpened = false
        override var settingsOpened = false
        override var registryEditorState: RegistryEditorState = RegistryEditorState.Closed
        override fun switchRegistry(registry: Registry) {}
        override fun addRecord(value1: Double, value2: Double?, pickerMillis: Long?) {}
        override fun removeRecord(recordItem: RecordItem) {}
        override fun isSelectableDate(utcTimeMillis: Long) = true
        override fun applyFilters(minViewValue: Double?, maxViewValue: Double?, goalValue: Double?, dateRange: Pair<Long, Long>?) = null
        override fun applyViewToggles(showGraph: Boolean, showList: Boolean) {}
        override fun updateTimeRange(range: TimeRange) {}
        override fun createRegistry(registry: Registry) {}
        override fun updateRegistry(registry: Registry) {}
        override fun deleteRegistry(registry: Registry) {}
    }
}
val OUTER_PADDING = 16.dp