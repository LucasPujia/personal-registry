package com.example.myapplication.mainActivity.weightSelector

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.example.myapplication.mainActivity.WEIGHT_MAX_VALUE
import com.example.myapplication.mainActivity.WEIGHT_MIN_VALUE
import com.example.myapplication.mainActivity.WEIGHT_PIXELS_PER_UNIT
import com.example.myapplication.mainActivity.WEIGHT_SCROLL_INVERTED
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Selector de número vertical tipo drum-wheel con movimiento fluido.
 *
 * - Arrastrar verticalmente para cambiar el valor.
 * - Muestra el valor central resaltado y previsualizaciones de los valores adyacentes.
 * - El movimiento es fluido y los números cambian de tamaño y opacidad al alejarse del centro.
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
    val step = remember(precision) { (10.0).pow(-precision).toFloat() }
    val scope = rememberCoroutineScope()
    
    // Animatable para el valor continuo con soporte para snapping fluido
    val continuousValue = remember { Animatable(value) }
    var isDragging by remember { mutableStateOf(false) }

    // Sincronizar con cambios externos (e.g. filtros/botones, así no salta sino que anima)
    LaunchedEffect(value) {
        if (!isDragging && abs(continuousValue.value - value) > 0.0001f) {
            continuousValue.animateTo(
                targetValue = value,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }
    }

    val draggableState = rememberDraggableState { delta ->
        val directionMultiplier = if (isScrollInverted) 1f else -1f
        val deltaUnits = directionMultiplier * delta / pixelsPerUnit
        val newValue = (continuousValue.value + deltaUnits).coerceIn(minValue, maxValue)
        
        scope.launch {
            continuousValue.snapTo(newValue)
        }
        
        val factor = (10.0).pow(precision)
        val snapped = ((newValue * factor).roundToInt() / factor).toFloat()
        if (snapped != value) {
            onValueChange(snapped)
        }
    }

    // Ancho medido del valor para ubicar 'kg' a su derecha sin afectar el centrado.
    var valueTextWidthPx by remember { mutableIntStateOf(500) }
    val density = LocalDensity.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = { isDragging = true },
                onDragStopped = { 
                    isDragging = false
                    // Al soltar, animamos hacia el valor actual con rebote visual (bouncy)
                    scope.launch {
                        continuousValue.animateTo(
                            targetValue = value,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                        )
                    }
                }
            ),
    ) {
        val itemHeight = 45.dp
        val kgOffsetX = (valueTextWidthPx / (2f * density.density)).dp + 16.dp
        
        // Calculamos el índice central y el rango de items a mostrar
        // Mayor arriba, menor abajo. 
        val centerValue = continuousValue.value
        val centerIndex = centerValue / step
        val startIndex = (centerIndex.roundToInt() - 3)
        val endIndex = (centerIndex.roundToInt() + 3)

        for (i in startIndex..endIndex) {
            val itemValue = i * step
            if (itemValue < minValue - 0.0001f || itemValue > maxValue + 0.0001f) continue

            // Para que el mayor esté ARRIBA, usamos (centerIndex - i)
            val distanceFromCenter = (centerValue / step) - i 
            val absDistance = abs(distanceFromCenter)

            // Interpolación de estilos agresiva para que el central destaque mucho más
            val scale = (1f - absDistance * 0.5f).coerceAtLeast(0.5f)
            val alpha = (1f - (absDistance * 0.3f)).coerceIn(0f, 1f)
            val yOffset = distanceFromCenter * itemHeight.value

            Text(
                text = "%.${precision}f".format(itemValue),
                fontSize = MaterialTheme.typography.displayLarge.fontSize,
                fontWeight = if (absDistance < 0.5f) FontWeight.Bold else FontWeight.SemiBold,
                color = Color(0xFF6750A4),
                onTextLayout = { 
                    if (absDistance < 0.5f) {
                        valueTextWidthPx = it.size.width 
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                        this.rotationX = -distanceFromCenter * 40f
                        this.translationY = yOffset * density.density
                    }
            )
        }

        Text(
            text = "kg",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6750A4).copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = kgOffsetX, y = 8.dp),
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