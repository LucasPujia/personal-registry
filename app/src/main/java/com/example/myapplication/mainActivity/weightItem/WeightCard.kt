package com.example.myapplication.mainActivity.weightItem

import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.mainActivity.MainActivityViewModel
import com.example.myapplication.mainActivity.weightsViewer.WeightDeletionState
import com.example.myapplication.utils.viewModelFromFloats

enum class DragValue { Settled, Revealed }

@Composable
fun WeightCard(
    item: WeightItem,
    previousWeight: Double?,
    viewModel: MainActivityViewModel,
    deletionState: WeightDeletionState
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
                    deletionState.openedItemKey = item.dateKey
                } else if (deletionState.openedItemKey == item.dateKey && target == DragValue.Settled) {
                    deletionState.openedItemKey = null
                }
            }
    }

    LaunchedEffect(deletionState.openedItemKey) {
        if (deletionState.openedItemKey != item.dateKey && state.currentValue == DragValue.Revealed) {
            state.animateTo(DragValue.Settled)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Acción de eliminar (fondo)
        CloseButton(deletionState, item, viewModel)

        // Contenido de la fila (frente)
        Surface(
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            ) {
                Surface(
                    color = Color(0xFFF3E5F5),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.getFullDateText(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        item.getDayOfWeekText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            item.formattedWeight(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    val diff = item.getDifferenceString(previousWeight)
                    if (diff.isNotEmpty()) {
                        Text(
                            text = diff,
                            color = when {
                                diff.startsWith("+") -> Color(0xFFD32F2F)
                                diff.startsWith("-") -> Color(0xFF388E3C)
                                else -> Color(0xFFFBC02D)
                            },
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
    deletionState: WeightDeletionState,
    item: WeightItem,
    viewModel: MainActivityViewModel
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp)
    ) {
        Button(
            onClick = { deletionState.askForDeletion(item, viewModel) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeightCardPreview() {
    val item = WeightItem(
        weight = 70.5,
        dateKey = "2024-06-15",
        date = "15/06"
    )

    WeightCard(
        item = item,
        previousWeight = 71.0,
        viewModel = viewModelFromFloats(listOf(70.5f)),
        deletionState = WeightDeletionState()
    )
}