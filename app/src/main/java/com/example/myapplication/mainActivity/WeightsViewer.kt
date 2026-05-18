package com.example.myapplication.mainActivity

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line

private enum class DragValue { Settled, Revealed }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeightsViewer(viewModel: MainActivityViewModel) {
    val isPreview = LocalInspectionMode.current
    val deletionState = rememberWeightDeletionState()

    ConfirmDeletionDialog(deletionState, viewModel)

    Column {
        if (viewModel.viewToggles.graph) LineChart(
            modifier = Modifier
                .height(250.dp)
                .padding(top = 24.dp),
            // Recomponer solo cuando se modifiquen los filtros, para eso es necesario
            // que sea una data class (sino habría que poner cada valor utilizado)
            data = remember(viewModel.filters) {
                buildList {
                    add(Line(
                        values = viewModel.filters.weightsD,
                        color = SolidColor(Color(0xFF23af92)),
                        firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .8f),
                        secondGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .3f),
                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                        gradientAnimationDelay = 1000,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                        curvedEdges = viewModel.filters.weights.size >= 32,
//                            popupProperties = PopupProperties(
//                                enabled = false,
//                            ),
                        dotProperties = DotProperties(
                            enabled = viewModel.filters.weights.size < 32,
                            radius = 4.dp,
                            color = SolidColor(Color(0xFF23af92))
                        )
                    ))
                    viewModel.filters.goalWeight?.let { goal -> add(
                        Line(
                            values = viewModel.filters.weights.map { goal.toDouble() },
                            color = SolidColor(Color(0xFFf57c00)),
                            strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )}
                }
            },
            animationMode = if (isPreview || !viewModel.filters.shouldAnimate) AnimationMode.None else AnimationMode.Together(
                delayBuilder = { it * 500L }
            ),
            labelHelperProperties = LabelHelperProperties(enabled = true, labelCountPerLine = 5, textStyle = MaterialTheme.typography.bodyMedium),
            minValue = viewModel.filters.minViewValue.toDouble(),
            maxValue = viewModel.filters.maxViewValue.toDouble(),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                labels = viewModel.filters.dateLabels,
                padding = 0.dp,
                rotation = LabelProperties.Rotation(
                    degree = -45f,
                    mode = LabelProperties.Rotation.Mode.Force
                )
            ),
            gridProperties = GridProperties(
                yAxisProperties = GridProperties.AxisProperties(lineCount = viewModel.filters.dateLabels.size.coerceAtLeast(2))
            )
        )

        if (viewModel.viewToggles.list) {
            HorizontalDivider(thickness = 3.dp, modifier = Modifier.padding(top = 32.dp))
            val weightsListReversed = remember(viewModel.filters.weights) {
                viewModel.filters.weights.reversed()
            }
            LazyColumn {
                itemsIndexed(
                    items = weightsListReversed,
                    key = { _, item -> item.dateKey }
                ) { index, item ->
                    WeightListItem(item, viewModel, deletionState)
                    if (index != weightsListReversed.size - 1) HorizontalDivider(thickness = 2.dp)
                }
            }
            HorizontalDivider(thickness = 3.dp)
        }
    }
}

@Composable
private fun WeightListItem(
    item: WeightItem,
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

    Box(modifier = Modifier.fillMaxWidth()) {
        // Acción de eliminar (fondo)
        Box(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
        ) {
            Button(
                onClick = { deletionState.askForDeletion(item, viewModel) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = MaterialTheme.shapes.small,
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

        // Contenido de la fila (frente)
        Surface(
            color = MaterialTheme.colorScheme.surface,
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text(text = item.formatted(), modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ConfirmDeletionDialog(
    deletionState: WeightDeletionState,
    viewModel: MainActivityViewModel
) {
    if (deletionState.weightToDelete == null) return
    AlertDialog(
        onDismissRequest = { deletionState.dismiss() },
        title = { Text("Confirmar eliminación") },
        text = {
            Column {
                Text("¿Estás seguro de que deseas borrar este registro?")
                if (deletionState.deletionCount >= 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("¿No volver a preguntar?", modifier = Modifier.weight(1f))
                        Switch(
                            checked = deletionState.dontAskAgainChecked,
                            onCheckedChange = { deletionState.dontAskAgainChecked = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { deletionState.confirmDeletion(viewModel) }) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = { deletionState.dismiss() }) {
                Text("Cancelar")
            }
        }
    )

}

@Composable
fun rememberWeightDeletionState(): WeightDeletionState {
    return rememberSaveable(saver = WeightDeletionState.Saver) {
        WeightDeletionState()
    }
}

class WeightDeletionState(
    initialDeletionCount: Int = 0,
    initialSkipConfirmation: Boolean = false
) {
    var weightToDelete by mutableStateOf<WeightItem?>(null)
    var deletionCount by mutableIntStateOf(initialDeletionCount)
    var skipConfirmation by mutableStateOf(initialSkipConfirmation)
    var dontAskAgainChecked by mutableStateOf(false)
    var openedItemKey by mutableStateOf<String?>(null)

    fun askForDeletion(item: WeightItem, viewModel: MainActivityViewModel) {
        if (skipConfirmation) {
            viewModel.removeWeight(item)
        } else {
            dontAskAgainChecked = false
            weightToDelete = item
        }
    }

    fun confirmDeletion(viewModel: MainActivityViewModel) {
        weightToDelete?.let {
            viewModel.removeWeight(it)
            deletionCount++
            if (dontAskAgainChecked) skipConfirmation = true
        }
        weightToDelete = null
    }

    fun dismiss() {
        weightToDelete = null
    }

    companion object {
        val Saver: Saver<WeightDeletionState, *> = listSaver(
            save = { listOf(it.deletionCount, it.skipConfirmation) },
            restore = { WeightDeletionState(it[0] as Int, it[1] as Boolean) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeightsViewerPreview() {
    val initialValues = listOf(25f, 30f, 35.5f, 32f, 28f, 29f)
    val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
    val mainActivityModel = MainActivityModel(memoryStorage)
    MaterialTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            WeightsViewer(MainActivityViewModel(mainActivityModel))
        }
    }
}