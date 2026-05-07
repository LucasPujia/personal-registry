package com.example.myapplication.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.database.weight.RoomWeightsStorage
import com.example.myapplication.utils.pressedInteractionSource2
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlin.math.pow
import kotlin.math.roundToInt


@Preview(showBackground = true)
@Composable
fun MyApplicationAppPreview() {
    MaterialTheme {
        val initialValues = listOf(25f, 30f, 35.5f)
        val memoryStorage = InMemoryWeightsStorage(initialValues)
        val mainActivityModel = MainActivityModel(memoryStorage)
        MyApplicationApp(MainActivityViewModel(mainActivityModel))
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel> {
        val database = AppDatabase.getInstance(applicationContext)
        val storage = RoomWeightsStorage(database.weightRecordDao())
        MainActivityViewModelFactory(MainActivityModel(storage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                MyApplicationApp(viewModel)
            }
        }
    }
}

@Composable
fun MyApplicationApp(viewModel: MainActivityViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .background(color = Color.White),
    ) {
        WeightSelector(viewModel)
        WeightsViewer(viewModel)
    }

    if (viewModel.filtersOpened) {
        FiltersBottomSheet(
            viewModel = viewModel,
            onDismissRequest = { viewModel.filtersOpened = false }
        )
    }
}

@Composable
private fun WeightSelector(
    viewModel: MainActivityViewModel,
) {
    val latestStoredWeight = viewModel.filters.weightsF.lastOrNull()
    var weight by remember(latestStoredWeight) {
        mutableFloatStateOf(latestStoredWeight ?: WEIGHT_DEFAULT_VALUE)
    }
    val weightStep = (10.0).pow(-WEIGHT_DECIMAL_PRECISION).toFloat()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Peso del día:",
                style = MaterialTheme.typography.titleMedium,
            )

            Row {
                val nextViewIconRes = if (viewModel.viewMode == ViewMode.CHART) {
                    android.R.drawable.ic_menu_agenda
                } else {
                    android.R.drawable.ic_menu_sort_by_size
                }


                FilledIconButton(
                    onClick = { viewModel.changeViewMode() },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = nextViewIconRes),
                        contentDescription = "Cambiar vista",
                    )
                }

                FilledIconButton(
                    onClick = { viewModel.filtersOpened = true },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_manage),
                        contentDescription = "Filtros",
                    )
                }
            }
        }

        VerticalNumberPicker(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledIconButton(
                onClick = { weight -= weightStep },
                interactionSource = pressedInteractionSource2 { weight -= weightStep },
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = "Decrementar peso",
                )
            }

            Button(
                onClick = { viewModel.addWeight(weight) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { Text("Agregar") }

            FilledIconButton(
                onClick = { weight += weightStep },
                interactionSource = pressedInteractionSource2 { weight += weightStep },
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_up_float),
                    contentDescription = "Aumentar peso",
                )
            }
        }
    }
}

@Composable
private fun WeightsViewer(viewModel: MainActivityViewModel) {
    val isPreview = LocalInspectionMode.current
    if (viewModel.viewMode == ViewMode.CHART) {
        var isFirstLoad by remember { mutableStateOf(true) }
        LineChart(
            modifier = Modifier
                .height(300.dp)
                .padding(top = 24.dp),
            data = buildList {
                add(Line(
                    values = viewModel.filters.weights,
                    color = SolidColor(Color(0xFF23af92)),
                    firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                    secondGradientFillColor = Color.Transparent,
                    strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                    gradientAnimationDelay = 1000,
                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                ))
                viewModel.filters.goalWeight?.let { goal ->
                    add(Line(
                        values = viewModel.filters.weights.map { goal.toDouble() },
                        color = SolidColor(Color(0xFFf57c00)),
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                    ))
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
        isFirstLoad = false
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


/**
 * Selector de número vertical tipo drum-wheel.
 *
 * - Arrastrar verticalmente para cambiar el valor.
 * - El movimiento es estrictamente **lineal**: cada [pixelsPerUnit] píxeles
 *   de arrastre cambia el valor en exactamente 1.0 unidad, sin aceleración.
 * - Muestra **un único número** a la vez; el valor se actualiza en tiempo real
 *   mientras el usuario arrastra.
 *
 * @param value         Valor actual.
 * @param onValueChange Callback llamado con el nuevo valor durante el arrastre.
 * @param precision     Decimales: 0 = enteros, 1 = pasos de 0.1, 2 = pasos de 0.01, etc.
 * @param pixelsPerUnit Píxeles de arrastre por 1.0 unidad de cambio (lineal, sin aceleración).
 * @param isScrollInverted Si es true, arrastrar hacia abajo aumenta el valor.
 * @param minValue      Valor mínimo permitido (inclusive).
 * @param maxValue      Valor máximo permitido (inclusive).
 */
@Composable
private fun VerticalNumberPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    precision: Int = WEIGHT_DECIMAL_PRECISION,
    pixelsPerUnit: Float = WEIGHT_PIXELS_PER_UNIT,
    isScrollInverted: Boolean = WEIGHT_SCROLL_INVERTED,
    minValue: Float = WEIGHT_MIN_VALUE,
    maxValue: Float = WEIGHT_MAX_VALUE,
) {
    // Valor en el momento en que inicia el gesto (referencia absoluta)
    var dragStartValue by remember { mutableFloatStateOf(value) }
    // Desplazamiento total acumulado en píxeles desde el inicio del gesto
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val draggableState = rememberDraggableState { delta ->
        dragAccumulator += delta
        val directionMultiplier = if (isScrollInverted) 1f else -1f
        val deltaUnits = directionMultiplier * dragAccumulator / pixelsPerUnit
        val rawNew = dragStartValue + deltaUnits
        // Ajustar al step más cercano (10^-precision) para evitar artefactos de punto flotante
        val factor = (10.0).pow(precision)
        val snapped = ((rawNew * factor).roundToInt() / factor).toFloat()
        onValueChange(snapped.coerceIn(minValue, maxValue))
    }

    // Ancho medido del valor para ubicar 'kg' a su derecha sin afectar el centrado.
    var valueTextWidthPx by remember { mutableIntStateOf(500) }
    val density = LocalDensity.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = {
                    dragStartValue = value
                    dragAccumulator = 0f
                },
                onDragStopped = {
                    dragAccumulator = 0f
                },
            ),
    ) {
        val kgOffsetX = (valueTextWidthPx / (2f * density.density)).dp + 16.dp

        Text(
            text = "%.${precision}f".format(value),
//            style = MaterialTheme.typography.displayLarge,
            fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.4f,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            onTextLayout = { valueTextWidthPx = it.size.width },
            modifier = Modifier.align(Alignment.Center),
        )

        Text(
            text = "kg",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = kgOffsetX, y = 16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersBottomSheet(
    viewModel: MainActivityViewModel,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    var minVal by remember { mutableStateOf(viewModel.filters.minViewValue.toString()) }
    var maxVal by remember { mutableStateOf(viewModel.filters.maxViewValue.toString()) }
    var goal by remember { mutableStateOf(viewModel.filters.goalWeight?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = minVal,
                onValueChange = { minVal = it },
                label = { Text("Mínimo (Gráfico)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = maxVal,
                onValueChange = { maxVal = it },
                label = { Text("Máximo (Gráfico)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text("Peso Objetivo") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    viewModel.applyFilters(
                        Filters(
                            minViewValue = minVal.toInt(),
                            maxViewValue = maxVal.toInt(),
                            goalWeight = goal.toIntOrNull()
                        )
                    )
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar")
            }
        }
    }
}
