package com.lucaspujia.personalregistry.mainActivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews

@Composable
fun RegistryFab(
    modifier: Modifier = Modifier
) {
    val viewModel = LocalMainActivityActions.current
    val registries by viewModel.allRegistries.collectAsState(initial = emptyList())
    val activeRegistry = viewModel.activeRegistry

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
    onRegistrySelected: (Registry) -> Unit,
    onCreateRegistryClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val centerOffset = calculateFabCenterOffset(fabSize = 64.dp, margin = 32.dp)
    val targetOffsetX = if (expanded) 0 else centerOffset

    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(targetOffsetX, 0),
        label = "fabOffset",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
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
                                    onRegistrySelected(registry)
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
                        onCreateRegistryClick()
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

@Composable
private fun calculateFabCenterOffset(fabSize: Dp, margin: Dp): Int {
    val windowInfo = LocalWindowInfo.current
    return with(LocalDensity.current) {
        val screenWidthPx = windowInfo.containerSize.width
        val fabSizePx = fabSize.toPx()
        val marginPx = margin.toPx()
        (screenWidthPx / 2 - fabSizePx / 2 - marginPx).toInt()
    }
}

@ThemePreviews
@Composable
private fun RegistryFabPreview() {
    val registries = listOf(
        Registry(id = 1, name = "Peso", emoji = "⚖️", unit1 = MeasureUnit("kg", "kg")),
        Registry(id = 2, name = "Ahorros", emoji = "💰", unit1 = MeasureUnit("usd", "$"))
    )
    PersonalRegistryTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            RegistryFabContent(
                registries = registries,
                activeRegistry = registries[0],
                onRegistrySelected = {},
                onCreateRegistryClick = {}
            )
        }
    }
}
