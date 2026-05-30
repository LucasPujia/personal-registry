package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lucaspujia.personalregistry.R

@Composable
fun NotificationsDialog(
    dismissDialog: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.select_theme)) },
        text = {
            Column {
                NotificationFrequency.entries.forEach { frequency ->
                    val isSelected = frequency == viewModel.notificationFrequency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateSetting(SettingOption.NOTIFICATION_FREQUENCY, frequency)
                                    dismissDialog()
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // El click lo maneja el Row
                        )
                        Text(
                            text = stringResource(frequency.messageId),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = dismissDialog) {
                Text(stringResource(R.string.close))
            }
        }
    )
}