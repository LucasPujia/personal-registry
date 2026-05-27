package com.lucaspujia.personalregistry.mainActivity.weightSelector

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.extensionFunctions.isFloat
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_MAX_VALUE
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_MIN_VALUE
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_PIXELS_PER_UNIT
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_SCROLL_INVERTED
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
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
            .background(MaterialTheme.colorScheme.surface)
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
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
            ),
    ) {
        val mainWeightHeight = 45.dp
        val kgOffsetX = remember(valueTextWidthPx, density) { (valueTextWidthPx / (2f * density.density)).dp + 16.dp }

        // Calculamos el índice central y el rango de items a mostrar
        // Mayor arriba, menor abajo.
        val centerIndexFloat = continuousValue.value / step
        val centerIndexInt = centerIndexFloat.roundToInt()
        val startIndex = centerIndexInt - 2
        val endIndex = centerIndexInt + 2

        for (i in startIndex..endIndex) {
            val itemValue = i * step
            if (itemValue < minValue - 0.0001f || itemValue > maxValue + 0.0001f) continue

            // Para que el mayor esté ARRIBA, usamos (centerIndex - i)
            val distanceFromCenter = centerIndexFloat - i
            val absDistance = abs(distanceFromCenter)
            val isMainWeight = i == centerIndexInt

            // Interpolación de estilos agresiva para que el central destaque mucho más
            val scale = (1f - absDistance * 0.5f).coerceAtLeast(0.5f)
            val alpha = (1f - (absDistance * 0.3f)).coerceIn(0f, 1f)
            val yOffset = distanceFromCenter * mainWeightHeight.value

            Text(
                text = "%.${precision}f".format(itemValue),
                fontSize = MaterialTheme.typography.displayLarge.fontSize,
                fontWeight = if (isMainWeight) FontWeight.Bold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                onTextLayout = { 
                    if (isMainWeight && valueTextWidthPx != it.size.width) {
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
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = kgOffsetX, y = 8.dp),
        )

        val showDialog = remember { mutableStateOf(false) }

        // Capa para detectar clic en el centro y abrir el modal
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(mainWeightHeight + 16.dp)
                .width(kgOffsetX + 64.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    showDialog.value = true
                }
        )

        WeightInputModal(showDialog, value.toString(), onValueChange, minValue, maxValue)
    }
}

@Composable
private fun WeightInputModal(
    showDialog: MutableState<Boolean>,
    initialValue: String,
    onValueChange: (Float) -> Unit,
    minValue: Float,
    maxValue: Float
) {
    if (showDialog.value) {
        var editValue by remember { mutableStateOf(initialValue) }
        var successfulEdit by remember { mutableStateOf(initialValue.isFloat()) }

        val closeDialog = { showDialog.value = false }
        val onConfirm = {
            // No lo puede parsear con coma, debe ser con punto
            val parsed = editValue.replace(',', '.').toFloatOrNull()
            if (parsed != null) {
                onValueChange(parsed.coerceIn(minValue, maxValue))
                closeDialog()
            } else {
                successfulEdit = false
            }
        }

        AlertDialog(
            onDismissRequest = closeDialog,
            text = {
                Column {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { if (it.isFloat()) {
                            editValue = it
                            successfulEdit = true
                        } },
                        label = { Text(stringResource(R.string.weight) + " (kg)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.fillMaxWidth(),
                        isError = !successfulEdit,
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        )
                    )
                    if (!successfulEdit) {
                        Text(
                            text = stringResource(R.string.invalid_weight),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@ThemePreviews
@Composable
fun VerticalNumberPickerPreview() {
    PersonalRegistryTheme {
        VerticalNumberPicker(
            value = 75.5f,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 300)
@Preview(showBackground = true, widthDp = 400, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WeightInputModalPreview() {
    PersonalRegistryTheme {
        val showDialog = remember { mutableStateOf(true) }
        WeightInputModal(
            showDialog = showDialog,
            initialValue = "70.0",
            onValueChange = {},
            minValue = WEIGHT_MIN_VALUE,
            maxValue = WEIGHT_MAX_VALUE
        )
    }
}
