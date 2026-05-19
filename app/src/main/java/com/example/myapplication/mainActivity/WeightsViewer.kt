package com.example.myapplication.mainActivity

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.weightItem.WeightItem
import com.example.myapplication.mainActivity.weightItem.WeightListItem
import com.example.myapplication.utils.OUTER_PADDING
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeightsViewer(
    viewModel: MainActivityViewModel,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val deletionState = rememberWeightDeletionState()

    ConfirmDeletionDialog(deletionState, viewModel)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FF))
    ) {
        if (viewModel.viewToggles.graph) {
            Surface(
                shape = RoundedCornerShape(OUTER_PADDING),
                color = Color.White,
                modifier = Modifier.padding(OUTER_PADDING)
            ) {
                Column(modifier = Modifier.padding(8.dp).padding(bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickFilters(
                            selectedRange = viewModel.currentTimeRange,
                            onRangeSelected = { viewModel.updateTimeRange(it) }
                        )
                    }
                    LineChart(
                        modifier = Modifier
                            .height(200.dp),
                        data = remember(viewModel.filters) {
                            buildList {
                                add(Line(
                                    values = viewModel.filters.weightsD,
                                    color = SolidColor(Color(0xFF6750A4)),
                                    firstGradientFillColor = Color(0xFF6750A4).copy(alpha = .3f),
                                    secondGradientFillColor = Color(0xFF6750A4).copy(alpha = .0f),
                                    strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                    gradientAnimationDelay = 1000,
                                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                                    curvedEdges = true,
                                    dotProperties = DotProperties(
                                        enabled = viewModel.filters.weights.size < 32,
                                        radius = 4.dp,
                                        color = SolidColor(Color(0xFF6750A4))
                                    )
                                ))
                                viewModel.filters.goalWeight?.let { goal ->
                                    add(Line(
                                        values = viewModel.filters.weights.map { goal.toDouble() },
                                        color = SolidColor(Color(0xFFf57c00)),
                                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                                    ))
                                }
                            }
                        },
                        animationMode = if (isPreview || !viewModel.filters.shouldAnimate) AnimationMode.None else AnimationMode.Together(
                            delayBuilder = { it * 500L }
                        ),
                        labelHelperProperties = LabelHelperProperties(
                            enabled = true,
                            labelCountPerLine = 5,
                            textStyle = MaterialTheme.typography.bodySmall
                        ),
                        minValue = viewModel.filters.minViewValue.toDouble(),
                        maxValue = viewModel.filters.maxViewValue.toDouble(),
                        labelProperties = LabelProperties(
                            enabled = true,
                            textStyle = MaterialTheme.typography.labelSmall,
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
                }
            }
        }

        if (viewModel.viewToggles.list) {
            HistorialHeader()
            val weightsListReversed = remember(viewModel.filters.weights) {
                viewModel.filters.weights.reversed()
            }
            LazyColumn(
                contentPadding = PaddingValues(bottom = OUTER_PADDING)
            ) {
                itemsIndexed(
                    items = weightsListReversed,
                    key = { _, item -> item.dateKey }
                ) { index, item ->
                    val previousItem = if (index + 1 < weightsListReversed.size) weightsListReversed[index + 1] else null
                    WeightListItem(item, previousItem?.weight, viewModel, deletionState)
                }
            }
        }
    }
}

@Composable
private fun QuickFilters(
    selectedRange: TimeRange?,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = selectedRange == range
            Surface(
                onClick = { onRangeSelected(range) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFF6750A4) else Color.Transparent,
                contentColor = if (isSelected) Color.White else Color.Gray,
            ) {
                Text(
                    text = range.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun HistorialHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Historial",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
//        TextButton(onClick = { /* TODO */ }) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    "Ver todo",
//                    style = MaterialTheme.typography.labelLarge,
//                    color = Color(0xFF6750A4)
//                )
//                Icon(
//                    Icons.Default.ChevronRight,
//                    contentDescription = null,
//                    tint = Color(0xFF6750A4),
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//        }
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