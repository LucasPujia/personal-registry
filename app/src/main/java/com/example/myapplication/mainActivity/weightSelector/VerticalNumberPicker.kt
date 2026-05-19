package com.example.myapplication.mainActivity.weightSelector

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.example.myapplication.mainActivity.WEIGHT_MAX_VALUE
import com.example.myapplication.mainActivity.WEIGHT_MIN_VALUE
import com.example.myapplication.mainActivity.WEIGHT_PIXELS_PER_UNIT
import com.example.myapplication.mainActivity.WEIGHT_SCROLL_INVERTED
import kotlin.math.pow
import kotlin.math.roundToInt

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
fun VerticalNumberPicker(
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
            .background(Color.White)
//            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
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
            color = Color(0xFF6750A4),
            onTextLayout = { valueTextWidthPx = it.size.width },
            modifier = Modifier.align(Alignment.Center),
        )

        Text(
            text = "kg",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6750A4).copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = kgOffsetX, y = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalNumberPickerPreview() {
    MaterialTheme {
        VerticalNumberPicker(
            value = 75.5f,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )
    }
}