package com.lucaspujia.personalregistry.mainActivity.recordItem

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.recordsViewer.RecordDeletionState
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.ui.theme.extendedColors

enum class DragValue { Settled, Revealed }

@Composable
fun RecordCard(
    recordItem: RecordItem,
    registry: Registry,
    deletionState: RecordDeletionState,
    variation: Double? = null,
) {
    val viewModel = LocalMainActivityActions.current
    RecordCardContent(
        recordItem = recordItem,
        registry = registry,
        variation = variation,
        deletionState = deletionState,
        onDeleteClick = { deletionState.askForDeletion(recordItem, onRemoveRecord = { viewModel.removeRecord(it) }) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordCardContent(
    recordItem: RecordItem,
    registry: Registry,
    variation: Double?,
    deletionState: RecordDeletionState,
    onDeleteClick: () -> Unit
) {
    val receiver = LocalDensity.current
    val state = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Settled,
            anchors = DraggableAnchors {
                DragValue.Settled at 0f
                DragValue.Revealed at -with(receiver) { 80.dp.toPx() }
            }
        )
    }

    LaunchedEffect(state) {
        snapshotFlow { state.targetValue }
            .collect { target ->
                if (target == DragValue.Revealed) {
                    deletionState.openedItemKey = recordItem.dateKey
                } else if ((deletionState.openedItemKey == recordItem.dateKey) && (target == DragValue.Settled)) {
                    deletionState.openedItemKey = null
                }
            }
    }

    LaunchedEffect(deletionState.openedItemKey) {
        if (deletionState.openedItemKey != recordItem.dateKey && state.currentValue == DragValue.Revealed) {
            state.animateTo(DragValue.Settled)
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Acción de eliminar (fondo)
        CloseButton(onDeleteClick)

        // Contenido de la fila (frente)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = state.requireOffset().toInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                        state = state,
                        positionalThreshold = { distance: Float -> distance * 0.5f },
                        animationSpec = tween()
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recordItem.getFullDateText(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        recordItem.getDayOfWeekText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            recordItem.formattedValue1(registry),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            registry.unit1.symbol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    recordItem.formattedValue2(registry)?.let { v2 ->
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                v2,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                registry.unit2?.symbol ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                    
                    variation?.let { v ->
                        val color = if (v > 0) {
                            MaterialTheme.extendedColors.trendIncrease
                        } else if (v < 0) {
                            MaterialTheme.extendedColors.trendDecrease
                        } else {
                            MaterialTheme.extendedColors.trendNeutral
                        }
                        Text(
                            text = recordItem.formattedVariation(registry, v) ?: "",
                            color = color,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CloseButton(
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp)
    ) {
        Button(
            onClick = onDeleteClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RecordCardPreview() {
    val registry = Registry(
        name = "Peso",
        emoji = "⚖️",
        unit1 = MeasureUnit("Kilo", "kg", 1)
    )
    val item = RecordItem(
        value1 = 70.5,
        dateKey = "2024-06-15"
    )
    PersonalRegistryTheme {
        RecordCardContent(
            recordItem = item,
            registry = registry,
            variation = -0.5,
            deletionState = RecordDeletionState(),
            onDeleteClick = {}
        )
    }
}

@ThemePreviews
@Composable
private fun RecordCardDoublePreview() {
    val registry = Registry(
        name = "Running",
        emoji = "🏃",
        unit1 = MeasureUnit("Distancia", "km", 2),
        unit2 = MeasureUnit("Tiempo", "min", 0)
    )
    val item = RecordItem(
        value1 = 5.23,
        value2 = 25.0,
        dateKey = "2024-06-15"
    )
    PersonalRegistryTheme {
        RecordCardContent(
            recordItem = item,
            registry = registry,
            variation = 0.1,
            deletionState = RecordDeletionState(),
            onDeleteClick = {}
        )
    }
}
