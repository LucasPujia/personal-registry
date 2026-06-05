package com.lucaspujia.personalregistry.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lucaspujia.personalregistry.mainActivity.bottomSheet.BottomSheetHandler
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.mainActivity.recordSelector.RecordSelector
import com.lucaspujia.personalregistry.mainActivity.recordsViewer.RecordsViewer
import com.lucaspujia.personalregistry.mainActivity.registry.CreateRegistryScreen
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsScreen
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsViewModel
import com.lucaspujia.personalregistry.ui.theme.DarkPreviewWithSystemUI
import com.lucaspujia.personalregistry.ui.theme.LightPreviewWithSystemUI
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.utils.OUTER_PADDING
import com.lucaspujia.personalregistry.utils.mockMainActivityViewModel
import com.lucaspujia.personalregistry.utils.recordsFromFloats
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PersonalRegistryTheme(themeMode = settingsViewModel.themeMode) {
                PersonalRegistryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalRegistryApp(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    CompositionLocalProvider(LocalMainActivityActions provides viewModel) {
        PersonalRegistryAppContent(
            records = viewModel.filters.records,
            settingsOpened = viewModel.settingsOpened,
            createRegistryOpened = viewModel.createRegistryOpened,
            hasActiveRegistry = viewModel.activeRegistry != null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalRegistryAppContent(
    records: List<RecordItem>,
    settingsOpened: Boolean,
    createRegistryOpened: Boolean,
    hasActiveRegistry: Boolean = true
) {
    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)) {
        if (hasActiveRegistry) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .statusBarsPadding(),
            ) {
                RecordSelector()
                if (records.isNotEmpty()) RecordsViewer(
                    modifier = Modifier.offset(y = -OUTER_PADDING),
                )
            }

            RegistryFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp, end = 32.dp)
            )
        }

        BottomSheetHandler()

        // TODO: ver de simplificar, junto al createRegistryOpended
        AnimatedVisibility(
            visible = settingsOpened,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            SettingsScreen()
        }

        AnimatedVisibility(
            visible = createRegistryOpened,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            CreateRegistryScreen()
        }
    }
}

@LightPreviewWithSystemUI
@DarkPreviewWithSystemUI
@Composable
fun PersonalRegistryAppPreview() {
    val floatValues = listOf(61f, 60f, 62f, 62f, 60f, 63f)
    val records = recordsFromFloats(floatValues)

    PersonalRegistryTheme(mainActivityViewModel = mockMainActivityViewModel(initialValues = floatValues)) {
        PersonalRegistryAppContent(
            records = records,
            settingsOpened = false,
            createRegistryOpened = false
        )
    }
}
