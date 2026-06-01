package com.lucaspujia.personalregistry.mainActivity.settings.dialogs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.settings.LocalSettingsActions
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationDay
import com.lucaspujia.personalregistry.mainActivity.settings.NotificationFrequency
import com.lucaspujia.personalregistry.mainActivity.settings.SettingOption
import com.lucaspujia.personalregistry.ui.theme.DialogPreviews
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme

@Composable
fun NotificationsDialog(
    dismissDialog: () -> Unit,
) {
    val viewModel = LocalSettingsActions.current
    NotificationsDialogContent(
        notificationFrequency = viewModel.notificationFrequency,
        notificationDay = viewModel.notificationDay,
        notificationHour = viewModel.notificationHour,
        notificationMinute = viewModel.notificationMinute,
        onFrequencyChange = { viewModel.updateSetting(SettingOption.NOTIFICATION_FREQUENCY, it) },
        onTimeChange = { hour, minute -> viewModel.updateNotificationTime(hour, minute) },
        onDayChange = { day -> viewModel.updateSetting(SettingOption.NOTIFICATION_DAY, day) },
        dismissDialog = dismissDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsDialogContent(
    notificationFrequency: NotificationFrequency,
    notificationDay: NotificationDay,
    notificationHour: Int,
    notificationMinute: Int,
    onFrequencyChange: (NotificationFrequency) -> Unit = {},
    onTimeChange: (Int, Int) -> Unit = {_,_ -> },
    onDayChange: (NotificationDay) -> Unit = {},
    dismissDialog: () -> Unit = {},
) {
    val context = LocalContext.current
    var pendingFrequency by remember { mutableStateOf<NotificationFrequency?>(null) }
    val showTimePicker = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        if (permissionGranted && pendingFrequency != null) {
            onFrequencyChange(pendingFrequency!!)
        } else {
            // TODO: Mostrar un toast. Integrar un servicio de toasts para solo tener que pasarle el texto y el color
        }
    }

    if (showTimePicker.value) {
        TimePickerDialog(
            notificationHour,
            notificationMinute,
            { showTimePicker.value = false },
            onTimeChange
        )
    }

    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.notifications)) },
        text = {
            Column {
                NotificationFrequency.entries.forEach { frequency ->
                    val isSelected = frequency == notificationFrequency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    if (frequency == NotificationFrequency.OFF || hasNotificationPermission(context)) {
                                        onFrequencyChange(frequency)
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

                if (notificationFrequency != NotificationFrequency.OFF) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = stringResource(R.string.notification_time),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = false,
                                onClick = { showTimePicker.value = true }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(LocalLocale.current.platformLocale, "%02d:%02d", notificationHour, notificationMinute),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                if (notificationFrequency == NotificationFrequency.DAYS_7) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = stringResource(R.string.notification_day),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PeriodSelector()
//                    MultiChoiceSegmentedButtonRow(
//
//                    ) {}
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotificationDay.entries.forEach { day ->
                            val isSelected = day == notificationDay
                            FilterChip(
                                selected = isSelected,
                                onClick = { onDayChange(day) },
                                label = { Text(stringResource(day.shortMessageId)) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = dismissDialog) {
                Text(stringResource(R.string.accept))
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerDialog(
    notificationHour: Int,
    notificationMinute: Int,
    closeTimePicker: () -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = notificationHour,
        initialMinute = notificationMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = closeTimePicker,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeChange(timePickerState.hour, timePickerState.minute)
                    closeTimePicker()
                }
            ) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = closeTimePicker) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

private fun hasNotificationPermission(context: Context): Boolean {
    val permissionValue = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    println("Notification permission value: $permissionValue")
    return permissionValue == PackageManager.PERMISSION_GRANTED
}

//TODO: mover a un ShowroomPreviews, con otros componentes genéricos de Jetpack Compose
@Composable
private fun PeriodSelector() {
    val options = listOf("Día", "Semana", "Mes")
    var selectedIndex by remember { mutableIntStateOf(0) }

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                selected = selectedIndex == index,
                onClick = { selectedIndex = index }
            ) {
                Text(label)
            }
        }
    }
}

// === PREVIEW ===
@Preview(showBackground = true, heightDp = 700, widthDp = 400)
@DialogPreviews
@Composable
private fun NotificationsDialogWeeklyPreview() {
    PersonalRegistryTheme {
        NotificationsDialogContent(
            notificationFrequency = NotificationFrequency.DAYS_7,
            notificationDay = NotificationDay.MONDAY,
            notificationHour = 8,
            notificationMinute = 30
        )
    }
}

@DialogPreviews
@Composable
private fun NotificationsDialogOffPreview() {
    PersonalRegistryTheme {
        NotificationsDialogContent(
            notificationFrequency = NotificationFrequency.OFF,
            notificationDay = NotificationDay.MONDAY,
            notificationHour = 8,
            notificationMinute = 30
        )
    }
}
