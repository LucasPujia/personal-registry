package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lucaspujia.personalregistry.R

interface Setting {
    val name: String
    val messageId: Int
}

enum class ThemeMode(override val messageId: Int) : Setting {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark);
}

enum class NotificationFrequency(override val messageId: Int) : Setting {
    OFF(R.string.notifications_off),
    DAYS_1(R.string.notifications_1d),
    DAYS_3(R.string.notifications_3d),
    DAYS_7(R.string.notifications_7d);
}

enum class NotificationDay(override val messageId: Int, val shortMessageId: Int) : Setting {
    MONDAY(R.string.monday, R.string.monday_short),
    TUESDAY(R.string.tuesday, R.string.tuesday_short),
    WEDNESDAY(R.string.wednesday, R.string.wednesday_short),
    THURSDAY(R.string.thursday, R.string.thursday_short),
    FRIDAY(R.string.friday, R.string.friday_short),
    SATURDAY(R.string.saturday, R.string.saturday_short),
    SUNDAY(R.string.sunday, R.string.sunday_short);
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
