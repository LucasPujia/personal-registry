package com.lucaspujia.personalregistry.mainActivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.RegistryIcon
import com.lucaspujia.personalregistry.utils.defaultMoneyRegistry
import com.lucaspujia.personalregistry.utils.defaultWeightRegistry

@Composable
fun RegistryFAB(
    modifier: Modifier = Modifier
) {
    val viewModel = LocalMainActivityActions.current
    val activeRegistry = viewModel.activeRegistry
    val registries by viewModel.allRegistries.collectAsState(initial = emptyList())

    RegistryFABContent(
        modifier = modifier,
        registries = registries,
        activeRegistry = activeRegistry!!,
        onRegistrySelected = { viewModel.switchRegistry(it) },
        onCreateRegistryClick = { viewModel.registryEditorState = RegistryEditorState.New },
        onEditRegistryClick = { viewModel.registryEditorState = RegistryEditorState.Edit(it) },
        onDeleteRegistryClick = { viewModel.deleteRegistry(it) }
    )
}

@Composable
private fun RegistryFABContent(
    modifier: Modifier = Modifier,
    registries: List<Registry>,
    activeRegistry: Registry,
    expanded: Boolean = false,
    onRegistrySelected: (Registry) -> Unit = {},
    onCreateRegistryClick: () -> Unit = {},
    onEditRegistryClick: (Registry) -> Unit = { },
    onDeleteRegistryClick: (Registry) -> Unit = { }
) {
    var expanded by remember { mutableStateOf(expanded) }
    val toggleExpanded = { expanded = !expanded }
    val baseSize = if (expanded) 64 else 48
    val basePadding = 10.dp
    val plusRotation by rotationAnimationState(expanded)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .height(baseSize.dp)
                    .padding(end = basePadding)
                    .clip(RoundedCornerShape((baseSize / 2).dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .padding(basePadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    { onDeleteRegistryClick(activeRegistry); toggleExpanded() },
                    plusRotation,
                    baseSize,
                    Icons.Default.Delete,
                    "Delete Registry"
                )
                ActionButton(
                    { onEditRegistryClick(activeRegistry); toggleExpanded() },
                    plusRotation,
                    baseSize,
                    Icons.Default.Edit,
                    "Edit Registry"
                )
            }
        }

        Column {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .width(baseSize.dp)
                        .padding(bottom = basePadding)
                        .clip(RoundedCornerShape((baseSize / 2).dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                        .padding(basePadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ActionButton(
                        { onCreateRegistryClick(); toggleExpanded() },
                        plusRotation,
                        baseSize,
                        Icons.Default.Add,
                        "Add Registry"
                    )

                    if (registries.size > 1) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(registries.filter { it.id != activeRegistry.id }) { registry ->
                                RegistryItem(baseSize, onRegistrySelected, registry, toggleExpanded)
                            }
                        }
                    }
                }
            }

            FloatingButton(toggleExpanded, activeRegistry, baseSize)
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    plusRotation: Float,
    baseSize: Int,
    icon: ImageVector,
    description: String
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        )
    ) {
        Icon(
            icon,
            contentDescription = description,
            modifier = Modifier.rotate(plusRotation).size((baseSize / 2 - 4).dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun RegistryItem(
    baseSize: Int,
    onRegistrySelected: (Registry) -> Unit,
    registry: Registry,
    toggleExpanded: () -> Unit
) {
    IconButton(
        onClick = {
            onRegistrySelected(registry)
            toggleExpanded()
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
        )
    ) {
        RegistryIcon(
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val fabSize by animateDpAsState(baseSize.dp)
    val cornerRadius by animateDpAsState((baseSize / 3).dp)

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
                textSize = (baseSize / 2).sp,
                tint = MaterialTheme.colorScheme.primary
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
)

@ThemePreviews
@Composable
private fun WeightRegistryFABPreview() {
    val registries = listOf(defaultWeightRegistry(), defaultMoneyRegistry(), defaultWeightRegistry().copy(id=3))
    PersonalRegistryTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            RegistryFABContent(
                registries = registries,
                activeRegistry = registries[0],
                expanded = true
            )
        }
    }
}
