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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.MainActivityViewModel
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.AboutDialog
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.ExportImportDialog
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.ImportConfirmationDialog
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.ImportErrorDialog
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.NotificationsDialog
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.SuccessDialog
import com.lucaspujia.personalregistry.mainActivity.settings.dialogs.ThemeSelectionDialog
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.mockSettingsViewModel

enum class SettingsCategory(val titleId: Int) {
    GENERAL(R.string.general),
    DATA(R.string.data),
    ABOUT(R.string.about)
}

enum class SettingsOption(
    val icon: ImageVector,
    val titleId: Int,
    val category: SettingsCategory,
    val content: @Composable (notificationFrequency: NotificationFrequency, themeMode: ThemeMode) -> String
) {
    MEASURE_UNIT(
        Icons.Default.Scale,
        R.string.measure_unit,
        category = SettingsCategory.GENERAL,
        content = { _, _ -> stringResource(MeasureUnit.METRIC.messageId) }),
    NOTIFICATIONS(
        Icons.Default.Notifications,
        R.string.notifications,
        category = SettingsCategory.GENERAL,
        content = { frequency, _ -> stringResource(frequency.messageId) }),
    THEME(
        Icons.Default.Palette,
        R.string.theme,
        category = SettingsCategory.GENERAL,
        content = { theme, _ -> stringResource(theme.messageId) }),
    EXPORT_IMPORT(
        Icons.Default.ImportExport,
        R.string.export_import,
        category = SettingsCategory.DATA,
        content = { _, _ -> stringResource(R.string.export_import_content) }
    ),
    ABOUT(
        Icons.Default.Info,
        R.string.about,
        category = SettingsCategory.ABOUT,
        content = { _, _ -> "${stringResource(R.string.version)} ${stringResource(R.string.app_version)}" })
}

enum class MeasureUnit(val messageId: Int) {
    METRIC(R.string.metric_kg_cm),
    IMPERIAL(R.string.imperial_lb_in)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainActivityViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    CompositionLocalProvider(LocalSettingsActions provides settingsViewModel) {
        SettingsScreenContent(
            modifier = modifier,
            onSettingsOpenedChange = { mainViewModel.settingsOpened = it },
            notificationFrequency = settingsViewModel.notificationFrequency,
            themeMode = settingsViewModel.themeMode,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    onSettingsOpenedChange: (Boolean) -> Unit = {},
    notificationFrequency: NotificationFrequency,
    themeMode: ThemeMode,
) {
    // Manejo del botón atrás del sistema
    BackHandler { onSettingsOpenedChange(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val showSettingDialog: MutableState<SettingsOption?> = remember { mutableStateOf(null) }
    val dismissDialog = { showSettingDialog.value = null }
    when (showSettingDialog.value) {
        SettingsOption.MEASURE_UNIT -> {}
        SettingsOption.NOTIFICATIONS -> NotificationsDialog(dismissDialog = dismissDialog)
        SettingsOption.THEME -> ThemeSelectionDialog(dismissDialog = dismissDialog)
        SettingsOption.EXPORT_IMPORT -> ExportImportDialog(dismissDialog = dismissDialog)
        SettingsOption.ABOUT -> AboutDialog(dismissDialog)
        else -> {}
    }

    ImportErrorDialog()
    ImportConfirmationDialog()
    SuccessDialog()

    Scaffold(
        topBar = {
            SettingsTitle(onSettingsOpenedChange, scrollBehavior)
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .closeOnLeftSlide(onSettingsOpenedChange)
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
                    CategoryTitle(category)
                }

                val optionsInCategory = SettingsOption.entries.filter { it.category == category }
                item(key = "${category.name}_group") {
                    SettingItemsColumn(
                        optionsInCategory,
                        notificationFrequency,
                        themeMode,
                        showSettingDialog
                    )
                }
            }
            // Para permitir el desplazamiento hacia arriba
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
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsTitle(
    onSettingsOpenedChange: (Boolean) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(onClick = { onSettingsOpenedChange(false) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun LazyItemScope.SettingItemsColumn(
    optionsInCategory: List<SettingsOption>,
    notificationFrequency: NotificationFrequency,
    themeMode: ThemeMode,
    showSettingDialog: MutableState<SettingsOption?>
) {
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
                SettingItem(
                    option,
                    notificationFrequency,
                    themeMode,
                    showSettingDialog
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

@Composable
private fun LazyItemScope.CategoryTitle(category: SettingsCategory) {
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

@Composable
private fun SettingItem(
    option: SettingsOption,
    notificationFrequency: NotificationFrequency,
    themeMode: ThemeMode,
    showSettingDialog: MutableState<SettingsOption?>
) {
    ListItem(
        headlineContent = { Text(stringResource(option.titleId)) },
        supportingContent = { Text(option.content(notificationFrequency, themeMode)) },
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
}

@Composable
private fun Modifier.closeOnLeftSlide(onSettingsOpenedChange: (Boolean) -> Unit): Modifier {
    return this.pointerInput(Unit) {
        var offsetX = 0f
        detectHorizontalDragGestures(
            onDragEnd = { offsetX = 0f },
            onDragCancel = { offsetX = 0f },
            onHorizontalDrag = { _, dragAmount ->
                offsetX += dragAmount
                if (offsetX > 200) { // Umbral para detectar el slide de salida
                    onSettingsOpenedChange(false)
                }
            }
        )
    }
}

@ThemePreviews
@Composable
private fun SettingsScreenPreview() {
    PersonalRegistryTheme {
        CompositionLocalProvider(LocalSettingsActions provides mockSettingsViewModel) {
            SettingsScreenContent(
                notificationFrequency = NotificationFrequency.OFF,
                themeMode = ThemeMode.SYSTEM,
            )
        }
    }
}

