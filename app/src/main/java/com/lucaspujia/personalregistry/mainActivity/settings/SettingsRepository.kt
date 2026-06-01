package com.lucaspujia.personalregistry.mainActivity.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(val context: Context) {

    val themeModeFlow by settingFlow<ThemeMode>(SettingOption.THEME)
    val notificationFrequencyFlow by settingFlow<NotificationFrequency>(SettingOption.NOTIFICATION_FREQUENCY)
    val notificationDayFlow by settingFlow<NotificationDay>(SettingOption.NOTIFICATION_DAY)

    val notificationHourFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[intPreferencesKey("notification_hour")] ?: 8 }
        .distinctUntilChanged()

    val notificationMinuteFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[intPreferencesKey("notification_minute")] ?: 0 }
        .distinctUntilChanged()

    suspend fun updateSetting(settingOption: SettingOption, setting: Setting) {
        context.dataStore.edit { preferences ->
            preferences[settingOption.key] = setting.name
        }
    }

    suspend fun updateNotificationHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey("notification_hour")] = hour
        }
    }

    suspend fun updateNotificationMinute(minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey("notification_minute")] = minute
        }
    }
}

class SettingFlowDelegate<T : Setting>(private val option: SettingOption) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: SettingsRepository, property: KProperty<*>): Flow<T> {
        return thisRef.context.dataStore.data
            .map { preferences ->
                val saved = preferences[option.key]
                (option.entries.find { it.name == saved } ?: option.defaultValue) as T
            }
            .distinctUntilChanged() // Evita emisiones si el valor es idéntico
    }
}

// Función de extensión para facilitar su uso
fun <T : Setting> settingFlow(option: SettingOption) = SettingFlowDelegate<T>(option)
