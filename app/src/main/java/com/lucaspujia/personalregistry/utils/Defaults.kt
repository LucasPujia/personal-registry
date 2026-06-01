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
import com.lucaspujia.personalregistry.mainActivity.settings.ImportExportState
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationDay
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import com.lucaspujia.personalregistry.mainActivity.settings.Setting
import com.lucaspujia.personalregistry.mainActivity.settings.SettingOption
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsActions
import com.lucaspujia.personalregistry.mainActivity.settings.ThemeMode

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

val OUTER_PADDING = 16.dp