package com.example.myapplication.mainActivity.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.mainActivity.MainActivityViewModel
import com.example.myapplication.mainActivity.ThemeMode

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

    val showThemeDialog = remember { mutableStateOf(false) }

    if (showThemeDialog.value) {
        ThemeSelectionDialog(
            viewModel = viewModel,
            showThemeDialog = showThemeDialog,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.settingsOpened = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
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
                ListItem(
                    headlineContent = { Text(stringResource(R.string.notifications)) },
                    supportingContent = { Text(stringResource(R.string.daily_reminder)) },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                ListItem(
                    headlineContent = {  Text(stringResource(R.string.theme)) },
                    supportingContent = { Text(stringResource(viewModel.themeMode.messageId)) },
                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                    modifier = Modifier.clickable { showThemeDialog.value = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                ListItem(
                    headlineContent = { 
                        Text(stringResource(R.string.about))
                    },
                    supportingContent = { 
                        Text(stringResource(R.string.version) + " 1.0.0")
                    },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    viewModel: MainActivityViewModel,
    showThemeDialog: MutableState<Boolean>
) {
    val onDismiss = { showThemeDialog.value = false }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_theme)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == viewModel.themeMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setTheme(mode)
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
                            text = stringResource(mode.messageId),
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

