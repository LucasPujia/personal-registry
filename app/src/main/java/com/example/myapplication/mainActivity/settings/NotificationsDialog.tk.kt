package com.example.myapplication.mainActivity.settings

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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.mainActivity.MainActivityViewModel

enum class NotificationFrequency(override val messageId: Int) : Setting {
    OFF(R.string.notifications_off),
    DAYS_1(R.string.notifications_1d),
    DAYS_3(R.string.notifications_3d),
    DAYS_7(R.string.notifications_7d);
}

@Composable
fun NotificationsDialog(
    viewModel: MainActivityViewModel,
    showSettingDialog: MutableState<SettingsOption?>
) {
    val onDismiss = { showSettingDialog.value = null }
    AlertDialog(
        onDismissRequest = onDismiss,
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
                                    onDismiss()
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}