package com.lucaspujia.personalregistry.mainActivity.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lucaspujia.personalregistry.R

@Composable
fun NotificationsDialog(
    dismissDialog: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var pendingFrequency by remember { mutableStateOf<NotificationFrequency?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        if (permissionGranted && pendingFrequency != null) {
            viewModel.updateSetting(SettingOption.NOTIFICATION_FREQUENCY, pendingFrequency!!)
        }
        dismissDialog()
    }

    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.notifications)) },
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
                                    if (frequency == NotificationFrequency.OFF || hasNotificationPermission(context)) {
                                        viewModel.updateSetting(SettingOption.NOTIFICATION_FREQUENCY, frequency)
                                        dismissDialog()
                                    } else {
                                        pendingFrequency = frequency
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
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

private fun hasNotificationPermission(context: Context): Boolean {
    val permissionValue = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    println("Notification permission value: $permissionValue")
    return permissionValue == PackageManager.PERMISSION_GRANTED
}