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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.lucaspujia.personalregistry.database.AppDatabase
import com.lucaspujia.personalregistry.database.weight.RoomWeightsStorage
import com.lucaspujia.personalregistry.mainActivity.bottomSheet.BottomSheetHandler
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsRepository
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsScreen
import com.lucaspujia.personalregistry.mainActivity.weightSelector.WeightSelector
import com.lucaspujia.personalregistry.mainActivity.weightsViewer.WeightsViewer
import com.lucaspujia.personalregistry.ui.theme.DarkPreviewWithSystemUI
import com.lucaspujia.personalregistry.ui.theme.LightPreviewWithSystemUI
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.utils.OUTER_PADDING
import com.lucaspujia.personalregistry.utils.viewModelFromFloats

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel> {
        val database = AppDatabase.getInstance(applicationContext)
        val storage = RoomWeightsStorage(database.weightRecordDao())
        val model = MainActivityModel(storage)
        val settingsRepository = SettingsRepository(applicationContext)
        MainActivityViewModelFactory(model, settingsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PersonalRegistryTheme(themeMode = viewModel.themeMode) {
                PersonalRegistryApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalRegistryApp(viewModel: MainActivityViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
        ) {
            WeightSelector(viewModel)
            if (viewModel.filters.weights.isNotEmpty()) WeightsViewer(
                viewModel,
                Modifier.offset(y = -OUTER_PADDING)
            )
        }

        BottomSheetHandler(viewModel)

        AnimatedVisibility(
            visible = viewModel.settingsOpened,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            SettingsScreen(viewModel)
        }
    }
}

@LightPreviewWithSystemUI
@DarkPreviewWithSystemUI
@Composable
fun PersonalRegistryAppPreview() {
    PersonalRegistryTheme {
        val viewModel = viewModelFromFloats(listOf(61f, 60f, 58f, 62f))
        PersonalRegistryApp(viewModel)
    }
}


