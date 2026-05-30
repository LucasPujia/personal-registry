package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lucaspujia.personalregistry.R
import java.io.BufferedWriter
import java.io.OutputStreamWriter

@Composable
fun ExportImportDialog(dismissDialog: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(viewModel.exportWeights())
                    }
                }
                dismissDialog()
            }
        }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { documentUri ->
            contentResolver.openInputStream(documentUri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { reader -> reader.readText() }
                viewModel.importWeights(json)
            }
            dismissDialog()
        }
    }

    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.export_import)) },
        text = { Text(stringResource(R.string.export_import_content)) },
        confirmButton = {
            TextButton(onClick = { exportLauncher.launch("weights.json") }) {
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
fun ImportErrorDialog(viewModel: SettingsViewModel = hiltViewModel()) {
    if (viewModel.importExportState.showError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportError() },
            title = { Text(stringResource(R.string.import_error_title)) },
            text = { Text(stringResource(R.string.import_error_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissImportError() }) {
                    Text(stringResource(R.string.accept))
                }
            }
        )
    }
}

@Composable
fun ImportConfirmationDialog(viewModel: SettingsViewModel = hiltViewModel()) {
    if (viewModel.importExportState.showConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportConfirmation() },
            title = { Text(stringResource(R.string.import_confirmation_title)) },
            text = { Text(stringResource(R.string.import_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmImport() }) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissImportConfirmation() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SuccessDialog(viewModel: SettingsViewModel = hiltViewModel()) {
    viewModel.importExportState.successMessageRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessMessage() },
            title = { Text(stringResource(R.string.success)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSuccessMessage() }) {
                    Text(stringResource(R.string.accept))
                }
            }
        )
    }
}
