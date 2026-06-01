package com.lucaspujia.personalregistry.mainActivity.settings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme

@Composable
fun AboutDialog(dismissDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.about)) },
        text = {
            Column {
                Text(
                    "${stringResource(R.string.app_name)} ${stringResource(R.string.version)} ${
                        stringResource(
                            R.string.app_version
                        )
                    }"
                )
                Text(stringResource(R.string.mit_license))
            }
        },
        confirmButton = {
            TextButton(onClick = dismissDialog) {
                Text(stringResource(R.string.accept))
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun AboutDialogPreview() {
    PersonalRegistryTheme {
        AboutDialog(dismissDialog = {})
    }
}