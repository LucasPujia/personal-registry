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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lucaspujia.personalregistry.mainActivity.bottomSheet.BottomSheetHandler
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsScreen
import com.lucaspujia.personalregistry.mainActivity.settings.SettingsViewModel
import com.lucaspujia.personalregistry.mainActivity.weightItem.WeightItem
import com.lucaspujia.personalregistry.mainActivity.weightSelector.WeightSelector
import com.lucaspujia.personalregistry.mainActivity.weightsViewer.WeightsViewer
import com.lucaspujia.personalregistry.ui.theme.DarkPreviewWithSystemUI
import com.lucaspujia.personalregistry.ui.theme.LightPreviewWithSystemUI
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.utils.OUTER_PADDING
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
            weights = viewModel.filters.weights,
            settingsOpened = viewModel.settingsOpened,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalRegistryAppContent(
    weights: List<WeightItem>,
    settingsOpened: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
        ) {
            WeightSelector()
            if (weights.isNotEmpty()) WeightsViewer(
                modifier = Modifier.offset(y = -OUTER_PADDING),
            )
        }

        BottomSheetHandler()

        AnimatedVisibility(
            visible = settingsOpened,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            SettingsScreen()
        }
    }
}

@LightPreviewWithSystemUI
@DarkPreviewWithSystemUI
@Composable
fun PersonalRegistryAppPreview() {
    val weights = listOf(
        WeightItem(61.0, "1", "1/1"),
        WeightItem(60.0, "2", "2/1")
    )
    // TODO: ver de moverlo a Defaults.kt
    val fakeActions = object : MainActivityActions {
        override val filters = ActiveFilters(weights = weights)
        override val viewToggles = ViewToggles()
        override val currentTimeRange = TimeRange.MONTH_1
        override var filtersOpened = false
        override var viewTogglesOpened = false
        override var settingsOpened = false
        override fun addWeight(weight: Float, pickerMillis: Long?) {}
        override fun removeWeight(weightItem: WeightItem) {}
        override fun isSelectableDate(utcTimeMillis: Long) = true
        override fun applyFilters(
            minViewValue: Int?,
            maxViewValue: Int?,
            goalWeight: Int?,
            dateRange: Pair<Long, Long>?
        ) = null

        override fun applyViewToggles(showGraph: Boolean, showList: Boolean) {}
        override fun updateTimeRange(range: TimeRange) {}
    }

    PersonalRegistryTheme {
        CompositionLocalProvider(LocalMainActivityActions provides fakeActions) {
            PersonalRegistryAppContent(
                weights = weights,
                settingsOpened = false,
            )
        }
    }
}


