package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lucaspujia.personalregistry.R
import java.time.DayOfWeek

interface Setting {
    val name: String
    val messageId: Int
}

enum class ThemeMode(override val messageId: Int) : Setting {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark);
}

enum class NotificationFrequency(override val messageId: Int, val days: Long) : Setting {
    OFF(R.string.notifications_off, -1),
    DAYS_1(R.string.notifications_1d, 1),
    DAYS_3(R.string.notifications_3d, 3),
    DAYS_7(R.string.notifications_7d, 7);
}

enum class NotificationDay(override val messageId: Int, val shortMessageId: Int, val dayOfWeek: DayOfWeek) : Setting {
    MONDAY(R.string.monday, R.string.monday_short, DayOfWeek.MONDAY),
    TUESDAY(R.string.tuesday, R.string.tuesday_short, DayOfWeek.TUESDAY),
    WEDNESDAY(R.string.wednesday, R.string.wednesday_short, DayOfWeek.WEDNESDAY),
    THURSDAY(R.string.thursday, R.string.thursday_short, DayOfWeek.THURSDAY),
    FRIDAY(R.string.friday, R.string.friday_short, DayOfWeek.FRIDAY),
    SATURDAY(R.string.saturday, R.string.saturday_short, DayOfWeek.SATURDAY),
    SUNDAY(R.string.sunday, R.string.sunday_short, DayOfWeek.SUNDAY);
}

enum class SettingOption(
    val key: Preferences.Key<String>,
    val defaultValue: Setting,
    val entries: List<Setting>,
) {
    THEME(
        stringPreferencesKey("theme_mode"),
        ThemeMode.SYSTEM,
        ThemeMode.entries
    ),
    NOTIFICATION_FREQUENCY(
        stringPreferencesKey("notification_frequency"),
        NotificationFrequency.OFF,
        NotificationFrequency.entries
    ),
    NOTIFICATION_DAY(
        stringPreferencesKey("notification_day"),
        NotificationDay.MONDAY,
        NotificationDay.entries
    );
}

interface SettingsActions {
    val themeMode: ThemeMode
    val notificationFrequency: NotificationFrequency
    val notificationDay: NotificationDay
    val notificationHour: Int
    val notificationMinute: Int
    val importExportState: ImportExportState

    fun updateSetting(settingOption: SettingOption, value: Setting)
    fun updateNotificationTime(hour: Int, minute: Int)
    fun exportRecords(registryId: Long): String
    fun importRecords(json: String, registryId: Long)
    fun confirmImport(registryId: Long)
    fun dismissImportError()
    fun dismissImportConfirmation()
}