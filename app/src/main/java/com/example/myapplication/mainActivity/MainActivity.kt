package com.example.myapplication.mainActivity

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.database.weight.RoomWeightsStorage
import com.example.myapplication.mainActivity.bottomSheet.BottomSheetHandler
import com.example.myapplication.mainActivity.settings.SettingsRepository
import com.example.myapplication.mainActivity.settings.SettingsScreen
import com.example.myapplication.mainActivity.weightSelector.WeightSelector
import com.example.myapplication.mainActivity.weightsViewer.WeightsViewer
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.OUTER_PADDING

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel> {
        val database = AppDatabase.getInstance(applicationContext)
        val storage = RoomWeightsStorage(database.weightRecordDao())
        val settingsRepository = SettingsRepository(applicationContext)
        val model = MainActivityModel(storage, settingsRepository)
        MainActivityViewModelFactory(model)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyApplicationTheme(themeMode = viewModel.themeMode) {
                MyApplicationApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(viewModel: MainActivityViewModel) {
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


@Preview(showSystemUi = true)
@Composable
fun MyApplicationAppPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    MyApplicationTheme(themeMode = ThemeMode.SYSTEM) {
        val initialValues = listOf(61f, 60f, 58f, 62f)
        val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
        val settingsRepository = SettingsRepository(context)
        val mainActivityModel = MainActivityModel(memoryStorage, settingsRepository)
        MyApplicationApp(MainActivityViewModel(mainActivityModel))
    }
}


