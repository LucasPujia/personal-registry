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
import com.lucaspujia.personalregistry.utils.recordsFromDoubles
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
    // TODO: Toasts para errores y éxitos
    // TODO: Modal de confirmación genérico con un service, implementar para eliminar registro
    // TODO: fórmula para 2 unidades
    // TODO: Revisar notificaciones, creo que no se están encolando más de una a la vez
    // TODO: Distintos textos para notificaciones
    // TODO: Extraer strings.xml en distintos archivos
    // TODO: ShowroomPreviews
    CompositionLocalProvider(LocalMainActivityActions provides viewModel) {
        PersonalRegistryAppContent(
            records = viewModel.filters.records,
            settingsOpened = viewModel.settingsOpened,
            registryEditorState = viewModel.registryEditorState,
            hasActiveRegistry = viewModel.activeRegistry != null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalRegistryAppContent(
    records: List<RecordItem>,
    settingsOpened: Boolean,
    registryEditorState: RegistryEditorState,
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

            RegistryFAB(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp, end = 32.dp)
            )
        }

        BottomSheetHandler()

        listOf(
            Pair(settingsOpened) @Composable { SettingsScreen() },
            Pair(!registryEditorState.isClosed()) @Composable { CreateRegistryScreen() }
        ).forEach { (isOpened, screen) ->
            AnimatedVisibility(
                visible = isOpened,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
            ) {
                screen()
            }
        }
    }
}

@LightPreviewWithSystemUI
@DarkPreviewWithSystemUI
@Composable
fun PersonalRegistryAppPreview() {
    val doubleValues = listOf(61.0, 60.0, 62.0, 62.0, 60.5, 63.0)
    val records = recordsFromDoubles(doubleValues)

    PersonalRegistryTheme(mainActivityViewModel = mockMainActivityViewModel(initialValues = doubleValues)) {
        PersonalRegistryAppContent(
            records = records,
            settingsOpened = false,
            registryEditorState = RegistryEditorState.Closed
        )
    }
}
