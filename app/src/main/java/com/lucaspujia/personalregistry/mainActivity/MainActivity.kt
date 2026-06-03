package com.lucaspujia.personalregistry.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            createRegistryOpened = viewModel.createRegistryOpened
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalRegistryAppContent(
    records: List<RecordItem>,
    settingsOpened: Boolean,
    createRegistryOpened: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)) {
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
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp, start = 32.dp)
        )

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

// TODO: rename y mover
@Composable
fun RegistryFab(modifier: Modifier = Modifier) {
    val viewModel = LocalMainActivityActions.current
    val registries by viewModel.allRegistries.collectAsState(initial = emptyList())
    val activeRegistry = viewModel.activeRegistry
    var expanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    // TODO: fix
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val marginPx = with(density) { 32.dp.toPx() }
    val fabSizeCollapsedPx = with(density) { 64.dp.toPx() }

    // Start at BottomCenter, move to BottomStart (which is the Alignment.BottomStart + padding)
    // When expanded is false, we want to be at Center.
    // Since we are aligned at BottomStart with 32dp padding, our x is already at 32dp.
    // Center of screen is screenWidthPx / 2.
    // To be at center, our left edge should be at screenWidthPx / 2 - fabSizeCollapsedPx / 2.
    // So the offset from our current position (32dp) is: (screenWidthPx / 2 - fabSizeCollapsedPx / 2) - 32dp.

    val centerOffset = (screenWidthPx / 2 - fabSizeCollapsedPx / 2 - marginPx).toInt()
    val targetOffsetX = if (expanded) 0 else centerOffset

    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(targetOffsetX, 0),
        label = "fabOffset",
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    val fabSize by animateDpAsState(if (expanded) 48.dp else 64.dp, label = "fabSize")
    val cornerRadius by animateDpAsState(if (expanded) 16.dp else 32.dp, label = "cornerRadius")

    Row(
        modifier = modifier
            .offset { animatedOffset },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(cornerRadius),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(fabSize),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = activeRegistry?.emoji ?: "📊",
                    fontSize = if (expanded) 24.sp else 32.sp
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(registries) { registry ->
                        val isSelected = registry.id == activeRegistry?.id
                        val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable {
                                    viewModel.switchRegistry(registry)
                                    expanded = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = registry.emoji, fontSize = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    onClick = { 
                        viewModel.createRegistryOpened = true
                        expanded = false
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Add Registry")
                    }
                }
            }
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
