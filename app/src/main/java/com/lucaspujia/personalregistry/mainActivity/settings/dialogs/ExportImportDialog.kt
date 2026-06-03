package com.lucaspujia.personalregistry.mainActivity.settings.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.settings.LocalSettingsActions
import com.lucaspujia.personalregistry.ui.theme.DialogPreviews
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import java.io.BufferedWriter
import java.io.OutputStreamWriter

@Composable
fun ExportImportDialog(dismissDialog: () -> Unit) {
    val settingsViewModel = LocalSettingsActions.current
    val mainViewModel = LocalMainActivityActions.current
    val activeRegistryId = mainViewModel.activeRegistry?.id ?: 1L

    ExportImportDialogContent(
        onImport = { json -> settingsViewModel.importRecords(json, activeRegistryId) },
        exportJsonProvider = { settingsViewModel.exportRecords(activeRegistryId) },
        dismissDialog = dismissDialog
    )
}

@Composable
private fun ExportImportDialogContent(
    onImport: (String) -> Unit = {},
    exportJsonProvider: () -> String = {""},
    dismissDialog: () -> Unit = {},
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                // TODO: Revisar si no debe hacerlo el viewModel
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(exportJsonProvider())
                    }
                }
                dismissDialog()
            }
        }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { documentUri ->
            contentResolver.openInputStream(documentUri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { reader -> reader.readText() }
                onImport(json)
            }
            dismissDialog()
        }
    }

    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.export_import)) },
        text = { Text(stringResource(R.string.export_import_content)) },
        confirmButton = {
            // TODO: nombre según el registro
            TextButton(onClick = { exportLauncher.launch("records.json") }) {
                Text(stringResource(R.string.export))
            }
        },
        dismissButton = {
            TextButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                Text(stringResource(R.string.import_data))
            }
        }
    )
}

@Composable
fun ImportErrorDialog() {
    val viewModel = LocalSettingsActions.current
    ImportErrorDialogContent(
        showError = viewModel.importExportState.showError,
        onDismiss = { viewModel.dismissImportError() }
    )
}

@Composable
private fun ImportErrorDialogContent(
    showError: Boolean,
    onDismiss: () -> Unit = {},
) {
    if (showError) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.import_error_title)) },
            text = { Text(stringResource(R.string.import_error_message)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.accept))
                }
            }
        )
    }
}

@Composable
fun ImportConfirmationDialog() {
    val settingsViewModel = LocalSettingsActions.current
    val mainViewModel = LocalMainActivityActions.current
    val activeRegistryId = mainViewModel.activeRegistry?.id ?: 1L

    ImportConfirmationDialogContent(
        showConfirmation = settingsViewModel.importExportState.showConfirmation,
        onConfirm = { settingsViewModel.confirmImport(activeRegistryId) },
        onDismiss = { settingsViewModel.dismissImportConfirmation() }
    )
}

@Composable
private fun ImportConfirmationDialogContent(
    showConfirmation: Boolean,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.import_confirmation_title)) },
            text = { Text(stringResource(R.string.import_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// TODO: Reemplazar por un toast
@Composable
fun SuccessDialog() {
    val viewModel = LocalSettingsActions.current
    SuccessDialogContent(
        successMessageRes = viewModel.importExportState.successMessageRes,
        onDismiss = { viewModel.dismissSuccessMessage() }
    )
}

@Composable
private fun SuccessDialogContent(
    successMessageRes: Int?,
    onDismiss: () -> Unit = {},
) {
    successMessageRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.success)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.accept))
                }
            }
        )
    }
}

@DialogPreviews
@Composable
private fun ExportImportDialogPreview() {
    PersonalRegistryTheme {
        ExportImportDialogContent(
        )
    }
}

@DialogPreviews
@Composable
private fun ImportErrorDialogPreview() {
    PersonalRegistryTheme {
        ImportErrorDialogContent(
            showError = true,
        )
    }
}

@DialogPreviews
@Composable
private fun ImportConfirmationDialogPreview() {
    PersonalRegistryTheme {
        ImportConfirmationDialogContent(
            showConfirmation = true,
        )
    }
}
