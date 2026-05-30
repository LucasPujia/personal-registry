package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.weight.WeightRecord
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val model: MainActivityModel,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    var themeMode by mutableStateOf(ThemeMode.SYSTEM); private set
    var notificationFrequency by mutableStateOf(NotificationFrequency.OFF); private set
    var importExportState by mutableStateOf(ImportExportState()); private set

    init {
        settingsRepository.themeModeFlow
            .onEach { themeMode = it }
            .launchIn(viewModelScope)

        settingsRepository.notificationFrequencyFlow
            .onEach { notificationFrequency = it }
            .launchIn(viewModelScope)
    }

    fun updateSetting(settingOption: SettingOption, value: Setting) {
        viewModelScope.launch {
            settingsRepository.updateSetting(settingOption, value)
        }
    }

    fun exportWeights(): String {
        val json = model.getRecordsAsJSON()
        importExportState = importExportState.copy(successMessageRes = R.string.export_success)
        return json
    }

    fun importWeights(json: String) {
        val records = model.fromRawJson(json)
        importExportState = if (records == null) {
            importExportState.copy(showError = true)
        } else {
            importExportState.copy(pendingRecords = records, showConfirmation = true)
        }
    }

    fun confirmImport() {
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

    fun dismissImportError() {
        importExportState = importExportState.copy(showError = false)
    }

    fun dismissImportConfirmation() {
        importExportState = importExportState.copy(showConfirmation = false, pendingRecords = emptyList())
    }

    fun dismissSuccessMessage() {
        importExportState = importExportState.copy(successMessageRes = null)
    }
}
