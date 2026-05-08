package com.example.myapplication.mainActivity

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun WeightsViewer(viewModel: MainActivityViewModel) {
    val isPreview = LocalInspectionMode.current
    if (viewModel.viewMode == ViewMode.CHART) {
        var isFirstLoad by remember { mutableStateOf(true) }
        LineChart(
            modifier = Modifier
                .height(300.dp)
                .padding(top = 24.dp),
            // Recomponer solo cuando se modifiquen los filtros, para eso es necesario
            // que sea una data class (sino habría que poner cada valor utilizado)
            data = remember(viewModel.filters) {
                buildList {
                    add(
                        Line(
                            values = viewModel.filters.weights,
                            color = SolidColor(Color(0xFF23af92)),
                            firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .8f),
                            secondGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .3f),
                            strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                            gradientAnimationDelay = 1000,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )
                    viewModel.filters.goalWeight?.let { goal ->
                        add(
                            Line(
                                values = viewModel.filters.weights.map { goal.toDouble() },
                                color = SolidColor(Color(0xFFf57c00)),
                                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                drawStyle = DrawStyle.Stroke(width = 2.dp),
                            )
                        )
                    }
                }
            },
            animationMode = if (isPreview || !isFirstLoad) AnimationMode.None else AnimationMode.Together(
                delayBuilder = { it * 500L }
            ),
            minValue = viewModel.filters.minViewValue.toDouble(),
            maxValue = viewModel.filters.maxViewValue.toDouble(),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                labels = listOf("Peso", "test"),
            ),
            gridProperties = GridProperties(
                yAxisProperties = GridProperties.AxisProperties(lineCount = 10)
            )
        )
        LaunchedEffect(Unit) {
            if (isFirstLoad) isFirstLoad = false
        }
    } else {
        LazyColumn {
            val weightsList = viewModel.filters.weights
            items(weightsList.size) { index ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "Peso ${index + 1}: ${
                            "%.${WEIGHT_DECIMAL_PRECISION}f".format(
                                weightsList[index]
                            )
                        } kg",
                    )
                    IconButton(
                        onClick = { viewModel.removeWeight(index) },
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
    }
}