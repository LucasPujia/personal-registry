package com.lucaspujia.personalregistry.mainActivity.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.MainActivityViewModel
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.viewModelFromFloats

enum class SettingsCategory(val titleId: Int) {
    GENERAL(R.string.general),
    DATA(R.string.data),
    ABOUT(R.string.about)
}

enum class SettingsOption(
    val icon: ImageVector,
    val titleId: Int,
    val category: SettingsCategory,
    val content: @Composable (viewModel: MainActivityViewModel) -> String
) {
    MEASURE_UNIT(
        Icons.Default.Scale,
        R.string.measure_unit,
        SettingsCategory.GENERAL,
        { stringResource(MeasureUnit.METRIC.messageId) }),
    NOTIFICATIONS(
        Icons.Default.Notifications,
        R.string.notifications,
        SettingsCategory.GENERAL,
        { viewModel -> stringResource(viewModel.notificationFrequency.messageId) }),
    THEME(
        Icons.Default.Palette,
        R.string.theme,
        SettingsCategory.GENERAL,
        { viewModel -> stringResource(viewModel.themeMode.messageId) }),
    EXPORT_IMPORT(
        Icons.Default.ImportExport,
        R.string.export_import,
        SettingsCategory.DATA,
        { stringResource(R.string.export_import_content) }
    ),
    ABOUT(
        Icons.Default.Info,
        R.string.about,
        SettingsCategory.ABOUT,
        { "${stringResource(R.string.version)} ${stringResource(R.string.app_version)}" })
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
    BackHandler { viewModel.settingsOpened = false }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.settingsOpened = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .closeOnLeftSlide(viewModel)
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsCategory.entries.forEach { category ->
                item(key = category.name) {
                    Text(
                        text = stringResource(category.titleId),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = tween(durationMillis = 500),
                                placementSpec = tween(durationMillis = 500)
                            )
                            .padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
                    )
                }

                val optionsInCategory = SettingsOption.entries.filter { it.category == category }
                item(key = "${category.name}_group") {
                    Surface(
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = tween(durationMillis = 500),
                                placementSpec = tween(durationMillis = 500)
                            )
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            optionsInCategory.forEachIndexed { index, option ->
                                ListItem(
                                    headlineContent = { Text(stringResource(option.titleId)) },
                                    supportingContent = { Text(option.content(viewModel)) },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = option.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier.clickable { showSettingDialog.value = option }
                                )
                                if (index < optionsInCategory.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier
                    .animateItem(
                        fadeInSpec = tween(durationMillis = 500),
                        placementSpec = tween(durationMillis = 500)
                    )
                    .height(32.dp))
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
private fun Modifier.closeOnLeftSlide(viewModel: MainActivityViewModel): Modifier {
    return this.pointerInput(Unit) {
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