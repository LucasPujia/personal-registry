package com.lucaspujia.personalregistry.mainActivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.RegistryIcon
import com.lucaspujia.personalregistry.utils.defaultMoneyRegistry
import com.lucaspujia.personalregistry.utils.defaultWeightRegistry

@Composable
fun RegistryFab(
    modifier: Modifier = Modifier
) {
    val viewModel = LocalMainActivityActions.current
    val activeRegistry = viewModel.activeRegistry
    val registries by viewModel.allRegistries.collectAsState(initial = emptyList())

    RegistryFabContent(
        modifier = modifier,
        registries = registries,
        activeRegistry = activeRegistry,
        onRegistrySelected = { viewModel.switchRegistry(it) },
        onCreateRegistryClick = { viewModel.createRegistryOpened = true }
    )
}

@Composable
private fun RegistryFabContent(
    modifier: Modifier = Modifier,
    registries: List<Registry>,
    activeRegistry: Registry?,
    expanded: Boolean = false,
    onRegistrySelected: (Registry) -> Unit = {},
    onCreateRegistryClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(expanded) }
    val toggleExpanded = { expanded = !expanded }
    val baseSize = if (expanded) 48 else 40
    val plusRotation by rotationAnimationState(expanded)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape((baseSize / 2).dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreateButton(onCreateRegistryClick, toggleExpanded, plusRotation)

                if (registries.size > 1) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(registries.filter { it.id != activeRegistry?.id }) { registry ->
                            RegistryItem(baseSize, onRegistrySelected, registry, toggleExpanded)
                        }
                    }
                }
            }
        }

        FloatingButton(toggleExpanded, activeRegistry, baseSize)
    }
}

@Composable
private fun CreateButton(
    onCreateRegistryClick: () -> Unit,
    toggleExpanded: () -> Unit,
    plusRotation: Float
) {
    Surface(
        onClick = {
            onCreateRegistryClick()
            toggleExpanded()
        },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Registry",
                modifier = Modifier.rotate(plusRotation)
            )
        }
    }
}

@Composable
private fun RegistryItem(
    baseSize: Int,
    onRegistrySelected: (Registry) -> Unit,
    registry: Registry,
    toggleExpanded: () -> Unit
) {
    Box(
        modifier = Modifier
            .size((baseSize * 3 / 4).dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .clickable {
                onRegistrySelected(registry)
                toggleExpanded()
            },
        contentAlignment = Alignment.Center
    ) {
        RegistryIcon(
            iconIdentifier = registry.emoji,
            contentDescription = null,
            textSize = (baseSize / 2).sp
        )
    }
}

@Composable
private fun FloatingButton(
    toggleExpanded: () -> Unit,
    activeRegistry: Registry?,
    baseSize: Int
) {
    val fabSize by animateDpAsState(baseSize.dp, label = "fabSize")
    val cornerRadius by animateDpAsState((baseSize / 3).dp, label = "cornerRadius")

    Surface(
        onClick = toggleExpanded,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(fabSize),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            RegistryIcon(
                iconIdentifier = activeRegistry?.emoji ?: "📊",
                contentDescription = null,
                textSize = (baseSize / 2).sp
            )
        }
    }
}

@Composable
private fun rotationAnimationState(expanded: Boolean): State<Float> = animateFloatAsState(
    targetValue = if (expanded) 0f else 45f,
    animationSpec = if (expanded) {
        tween(durationMillis = 300, delayMillis = 100)
    } else {
        tween(durationMillis = 300)
    },
    label = "plusRotation"
)

@ThemePreviews
@Composable
private fun WeightRegistryFabPreview() {
    val registries = listOf(defaultWeightRegistry(), defaultMoneyRegistry())
    PersonalRegistryTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            RegistryFabContent(
                registries = registries,
                activeRegistry = registries[0],
                expanded = true
            )
        }
    }
}
