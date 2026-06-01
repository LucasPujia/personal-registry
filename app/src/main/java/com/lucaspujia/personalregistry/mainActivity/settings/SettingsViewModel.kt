package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.weight.WeightRecord
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import com.lucaspujia.personalregistry.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ImportExportState(
    val showError: Boolean = false,
    val showConfirmation: Boolean = false,
    val successMessageRes: Int? = null,
    val pendingRecords: List<WeightRecord> = emptyList()
)

// TODO: Moverlo al settingsModel, y ver de renombrar el settingsModel
interface SettingsActions {
    val themeMode: ThemeMode
    val notificationFrequency: NotificationFrequency
    val notificationDay: NotificationDay
    val notificationHour: Int
    val notificationMinute: Int
    val importExportState: ImportExportState

    fun updateSetting(settingOption: SettingOption, value: Setting)
    fun updateNotificationTime(hour: Int, minute: Int)
    fun exportWeights(): String
    fun importWeights(json: String)
    fun confirmImport()
    fun dismissImportError()
    fun dismissImportConfirmation()
    fun dismissSuccessMessage()
}

val LocalSettingsActions = staticCompositionLocalOf<SettingsActions> {
    error("No SettingsActions provided")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val model: MainActivityModel,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel(), SettingsActions {

    override var themeMode by mutableStateOf(SettingOption.THEME.defaultValue as ThemeMode); private set
    override var notificationFrequency by mutableStateOf(SettingOption.NOTIFICATION_FREQUENCY.defaultValue as NotificationFrequency); private set
    override var notificationDay by mutableStateOf(SettingOption.NOTIFICATION_DAY.defaultValue as NotificationDay); private set
    override var notificationHour by mutableIntStateOf(8); private set
    override var notificationMinute by mutableIntStateOf(0); private set
    override var importExportState by mutableStateOf(ImportExportState()); private set

    init {
        settingsRepository.themeModeFlow
            .onEach { themeMode = it }
            .launchIn(viewModelScope)

        combine(
            settingsRepository.notificationFrequencyFlow,
            settingsRepository.notificationDayFlow,
            settingsRepository.notificationHourFlow,
            settingsRepository.notificationMinuteFlow
        ) { frequency, day, hour, minute ->
            Triple(frequency, day, hour to minute)
        }.onEach { (frequency, day, hourMinute) ->
            notificationFrequency = frequency
            notificationDay = day
            notificationHour = hourMinute.first
            notificationMinute = hourMinute.second
            notificationScheduler.scheduleNotification(frequency, hourMinute.first, hourMinute.second)
        }.launchIn(viewModelScope)
    }

    override fun updateSetting(settingOption: SettingOption, value: Setting) {
        viewModelScope.launch {
            settingsRepository.updateSetting(settingOption, value)
        }
    }

    override fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.updateNotificationHour(hour)
            settingsRepository.updateNotificationMinute(minute)
        }
    }

    override fun exportWeights(): String {
        val json = model.getRecordsAsJSON()
        importExportState = importExportState.copy(successMessageRes = R.string.export_success)
        return json
    }

    override fun importWeights(json: String) {
        val records = model.fromRawJson(json)
        importExportState = if (records == null) {
            importExportState.copy(showError = true)
        } else {
            importExportState.copy(pendingRecords = records, showConfirmation = true)
        }
    }

    override fun confirmImport() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                model.replaceWeights(importExportState.pendingRecords)
            }
            importExportState = importExportState.copy(
                showConfirmation = false,
                pendingRecords = emptyList(),
                successMessageRes = R.string.import_success
            )
        }
    }

    override fun dismissImportError() {
        importExportState = importExportState.copy(showError = false)
    }

    override fun dismissImportConfirmation() {
        importExportState = importExportState.copy(showConfirmation = false, pendingRecords = emptyList())
    }

    override fun dismissSuccessMessage() {
        importExportState = importExportState.copy(successMessageRes = null)
    }
}
