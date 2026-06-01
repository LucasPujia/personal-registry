package com.lucaspujia.personalregistry.mainActivity.settings.dialogs

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
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.settings.LocalSettingsActions
import com.lucaspujia.personalregistry.mainActivity.settings.SettingOption
import com.lucaspujia.personalregistry.mainActivity.settings.ThemeMode
import com.lucaspujia.personalregistry.ui.theme.DialogPreviews
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme

@Composable
fun ThemeSelectionDialog(
    dismissDialog: () -> Unit,
) {
    val viewModel = LocalSettingsActions.current
    ThemeSelectionDialogContent(
        selectedThemeMode = viewModel.themeMode,
        onThemeModeSelected = {
            viewModel.updateSetting(SettingOption.THEME, it)
            dismissDialog()
        },
        dismissDialog = dismissDialog
    )
}

@Composable
private fun ThemeSelectionDialogContent(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    dismissDialog: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.select_theme)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == selectedThemeMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onThemeModeSelected(mode) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // El click lo maneja el Row
                        )
                        Text(
                            text = stringResource(mode.messageId),
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

@DialogPreviews
@Composable
private fun ThemeSelectionDialogPreview() {
    PersonalRegistryTheme {
        ThemeSelectionDialogContent(
            selectedThemeMode = ThemeMode.DARK,
            onThemeModeSelected = {},
            dismissDialog = {}
        )
    }
}
