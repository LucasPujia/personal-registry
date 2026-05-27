package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.MainActivityViewModel
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.viewModelFromFloats

enum class SettingsOption(val messageId: Int) {
    MEASURE_UNIT(R.string.measure_unit),
    NOTIFICATIONS(R.string.notifications),
    THEME(R.string.theme),
    ABOUT(R.string.about)
}

enum class MeasureUnit(val messageId: Int) {
    METRIC(R.string.metric_kg_cm),
    IMPERIAL(R.string.imperial_lb_in)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainActivityViewModel,
    modifier: Modifier = Modifier
) {
    // Manejo del botón atrás del sistema
    BackHandler {
        viewModel.settingsOpened = false
    }

    val showSettingDialog: MutableState<SettingsOption?> = remember { mutableStateOf(null) }
    val dismissDialog = { showSettingDialog.value = null }
    when (showSettingDialog.value) {
        SettingsOption.MEASURE_UNIT -> {}
        SettingsOption.NOTIFICATIONS -> NotificationsDialog(viewModel = viewModel, dismissDialog = dismissDialog)
        SettingsOption.THEME -> ThemeSelectionDialog(viewModel = viewModel, dismissDialog = dismissDialog)
        SettingsOption.ABOUT -> AboutDialog(dismissDialog)
        else -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.settingsOpened = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.closeOnLeftSlide(viewModel).fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.measure_unit)) },
                    supportingContent = { Text(stringResource(MeasureUnit.METRIC.messageId)) },
                    leadingContent = { Icon(Icons.Default.Scale, contentDescription = null) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                val notificationsEnabled = remember { mutableStateOf(true) }
                ListItem(
                    headlineContent = { Text(stringResource(R.string.notifications)) },
                    supportingContent = { Text(stringResource(viewModel.notificationFrequency.messageId)) },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = { Switch(checked = notificationsEnabled.value, onCheckedChange = { notificationsEnabled.value = it }) },
                    modifier = Modifier.clickable { showSettingDialog.value = SettingsOption.NOTIFICATIONS },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                ListItem(
                    headlineContent = {  Text(stringResource(R.string.theme)) },
                    supportingContent = { Text(stringResource(viewModel.themeMode.messageId)) },
                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                    modifier = Modifier.clickable { showSettingDialog.value = SettingsOption.THEME }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about)) },
                    supportingContent = { Text("${stringResource(R.string.version)} ${stringResource(R.string.app_version)}") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.clickable { showSettingDialog.value = SettingsOption.ABOUT }
                )
            }
        }
    }
}

@Composable
private fun AboutDialog(dismissDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismissDialog,
        title = { Text(stringResource(R.string.about)) },
        text = {
            Column {
                Text("${stringResource(R.string.app_name)} ${stringResource(R.string.version)} ${stringResource(R.string.app_version)}")
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

@Composable
private fun Modifier.closeOnLeftSlide(
    viewModel: MainActivityViewModel
): Modifier = this
    .pointerInput(Unit) {
        var offsetX = 0f
        detectHorizontalDragGestures(
            onDragEnd = { offsetX = 0f },
            onDragCancel = { offsetX = 0f },
            onHorizontalDrag = { _, dragAmount ->
                offsetX += dragAmount
                if (offsetX > 200) { // Umbral para detectar el slide de salida
                    viewModel.settingsOpened = false
                }
            }
        )
    }

@ThemePreviews
@Composable
fun SettingsScreenPreview() {
    PersonalRegistryTheme {
        SettingsScreen(viewModel = viewModelFromFloats(listOf()))
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 300)
@Composable
fun AboutDialogPreview() {
    val showSettingDialog = remember<MutableState<SettingsOption?>> { mutableStateOf(SettingsOption.ABOUT) }
    PersonalRegistryTheme {
        AboutDialog(dismissDialog = {showSettingDialog.value = null})
    }
}