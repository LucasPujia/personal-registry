package com.example.myapplication.mainActivity

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun WeightsViewer(viewModel: MainActivityViewModel) {
    val isPreview = LocalInspectionMode.current
    Column() {
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
            val weightsList = remember(viewModel.filters.weights) {
                viewModel.filters.weights.reversed()
            }
            LazyColumn {
                val weightsList = viewModel.filters.weights
                items(weightsList.size) { index ->
                    val reversedIndex = weightsList.lastIndex - index
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            text = weightsList[reversedIndex].formatted(),
                        )
                        IconButton(
                            onClick = { viewModel.removeWeight(weightsList[reversedIndex]) },
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_delete),
                                contentDescription = "Eliminar peso",
                                tint = Color.Red,
                            )
                        }
                    }
                    if (index != weightsList.size - 1) HorizontalDivider(thickness = 2.dp)
                }
            }
            HorizontalDivider(thickness = 3.dp)
        }
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