package com.lucaspujia.personalregistry.mainActivity.recordsViewer

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.ActiveFilters
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.TimeRange
import com.lucaspujia.personalregistry.mainActivity.ViewToggles
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordCard
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.OUTER_PADDING
import com.lucaspujia.personalregistry.utils.defaultWeightRegistry
import com.lucaspujia.personalregistry.utils.recordsFromFloats
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordsViewer(
    modifier: Modifier = Modifier,
) {
    val viewModel = LocalMainActivityActions.current
    val registry = viewModel.activeRegistry ?: return

    RecordsViewerContent(
        modifier = modifier,
        registry = registry,
        viewToggles = viewModel.viewToggles,
        currentTimeRange = viewModel.currentTimeRange,
        filters = viewModel.filters,
        onRangeSelected = { viewModel.updateTimeRange(it) },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordsViewerContent(
    modifier: Modifier = Modifier,
    registry: Registry,
    viewToggles: ViewToggles,
    currentTimeRange: TimeRange?,
    filters: ActiveFilters,
    onRangeSelected: (TimeRange) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    val deletionState = rememberRecordDeletionState()

    ConfirmDeletionDialog(deletionState = deletionState)

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = OUTER_PADDING)
    ) {
        if (viewToggles.graph && filters.records.size > 1) {
            Surface(
                shape = RoundedCornerShape(OUTER_PADDING),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(top = OUTER_PADDING)
            ) {
                Column(modifier = Modifier.padding(8.dp).padding(bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickFilters(
                            selectedRange = currentTimeRange,
                            onRangeSelected = onRangeSelected
                        )
                    }
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val textColor = MaterialTheme.colorScheme.onSurface
                    LineChart(
                        modifier = Modifier
                            .height(200.dp),
                        data = remember(filters, primaryColor, secondaryColor) {
                            buildList {
                                add(Line(
                                    values = filters.values1D,
                                    color = SolidColor(primaryColor),
                                    firstGradientFillColor = primaryColor.copy(alpha = .3f),
                                    secondGradientFillColor = primaryColor.copy(alpha = .0f),
                                    strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                    gradientAnimationDelay = 1000,
                                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                                    curvedEdges = true,
                                    dotProperties = DotProperties(
                                        enabled = filters.records.size < 32,
                                        radius = 4.dp,
                                        color = SolidColor(primaryColor)
                                    )
                                ))
                                if (registry.unit2 != null) {
                                    add(Line(
                                        values = filters.records.map { it.value2 ?: 0.0 },
                                        color = SolidColor(secondaryColor),
                                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                                        curvedEdges = true,
                                        dotProperties = DotProperties(
                                            enabled = filters.records.size < 32,
                                            radius = 4.dp,
                                            color = SolidColor(secondaryColor)
                                        )
                                    ))
                                }
                                filters.goalValue?.let { goal ->
                                    add(Line(
                                        values = filters.records.map { goal.toDouble() },
                                        color = SolidColor(secondaryColor.copy(alpha = 0.5f)),
                                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                        drawStyle = DrawStyle.Stroke(width = 1.dp),
                                    ))
                                }
                            }
                        },
                        animationMode = if (isPreview || !filters.shouldAnimate) AnimationMode.None else AnimationMode.Together(
                            delayBuilder = { it * 500L }
                        ),
                        labelHelperProperties = LabelHelperProperties(
                            enabled = true,
                            labelCountPerLine = 5,
                        ),
                        minValue = filters.minViewValue.toDouble(),
                        maxValue = filters.maxViewValue.toDouble(),
                        labelProperties = LabelProperties(
                            enabled = true,
                            textStyle = MaterialTheme.typography.labelSmall.copy(color = textColor),
                            labels = filters.dateLabels,
                            padding = 0.dp,
                            rotation = LabelProperties.Rotation(
                                degree = -45f,
                                mode = LabelProperties.Rotation.Mode.Force
                            )
                        ),
                        indicatorProperties = HorizontalIndicatorProperties(
                            textStyle = TextStyle.Default.copy(color = textColor),
                            padding = 16.dp
                        ),
                        gridProperties = GridProperties(
                            enabled = true,
                            xAxisProperties = GridProperties.AxisProperties(color = SolidColor(textColor)),
                            yAxisProperties = GridProperties.AxisProperties(
                                lineCount = filters.dateLabels.size.coerceAtLeast(2),
                                color = SolidColor(textColor)
                            )
                        )
                    )
                }
            }
        }

        if (viewToggles.list) {
            Column(modifier = Modifier.padding(top = OUTER_PADDING)) {
                HistorialHeader()
                val recordsWithVariation = remember(filters.records, registry) {
                    filters.records.mapIndexed { index, record ->
                        val previous = if (index > 0) filters.records[index - 1] else null
                        record to record.calculateVariation(registry, previous)
                    }.reversed()
                }
                LazyColumn {
                    items(
                        items = recordsWithVariation,
                        key = { (item, _) -> item.id }
                    ) { (item, variation) ->
                        RecordCard(
                            recordItem = item,
                            registry = registry,
                            deletionState = deletionState,
                            variation = variation
                        )
                    }
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
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.history),
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfirmDeletionDialog(
    deletionState: RecordDeletionState,
) {
    val viewModel = LocalMainActivityActions.current
    ConfirmDeletionDialogContent(
        deletionState = deletionState,
        onConfirm = { viewModel.removeRecord(it) }
    )
}

@Composable
private fun ConfirmDeletionDialogContent(
    deletionState: RecordDeletionState,
    onConfirm: (RecordItem) -> Unit = {}
) {
    if (deletionState.recordToDelete == null) return
    AlertDialog(
        onDismissRequest = { deletionState.dismiss() },
        title = { Text(stringResource(R.string.confirm_deletion)) },
        text = {
            Column {
                Text(stringResource(R.string.confirm_deletion_question))
                if (deletionState.deletionCount >= 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.dont_ask_again), modifier = Modifier.weight(1f))
                        Switch(
                            checked = deletionState.dontAskAgainChecked,
                            onCheckedChange = { deletionState.dontAskAgainChecked = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { deletionState.confirmDeletion(onConfirm) }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { deletionState.dismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun rememberRecordDeletionState(): RecordDeletionState {
    return rememberSaveable(saver = RecordDeletionState.Saver) {
        RecordDeletionState()
    }
}

class RecordDeletionState(
    initialDeletionCount: Int = 0,
    initialSkipConfirmation: Boolean = false
) {
    var recordToDelete by mutableStateOf<RecordItem?>(null)
    var deletionCount by mutableIntStateOf(initialDeletionCount)
    var skipConfirmation by mutableStateOf(initialSkipConfirmation)
    var dontAskAgainChecked by mutableStateOf(false)
    var openedItemKey by mutableStateOf<String?>(null)

    fun askForDeletion(item: RecordItem, onRemoveRecord: (RecordItem) -> Unit) {
        if (skipConfirmation) {
            onRemoveRecord(item)
        } else {
            dontAskAgainChecked = false
            recordToDelete = item
        }
    }

    fun confirmDeletion(onRemoveRecord: (RecordItem) -> Unit) {
        recordToDelete?.let {
            onRemoveRecord(it)
            deletionCount++
            if (dontAskAgainChecked) skipConfirmation = true
        }
        recordToDelete = null
    }

    fun dismiss() {
        recordToDelete = null
    }

    companion object {
        val Saver: Saver<RecordDeletionState, *> = listSaver(
            save = { listOf(it.deletionCount, it.skipConfirmation) },
            restore = { RecordDeletionState(it[0] as Int, it[1] as Boolean) }
        )
    }
}


@ThemePreviews
@Composable
fun RecordsViewerPreview() {
    val filters = ActiveFilters(
        records = recordsFromFloats(listOf(70.0f, 69.5f))
    )
    PersonalRegistryTheme {
        RecordsViewerContent(
            registry = defaultWeightRegistry(),
            viewToggles = ViewToggles(),
            currentTimeRange = TimeRange.MONTH_1,
            filters = filters
        )
    }
}