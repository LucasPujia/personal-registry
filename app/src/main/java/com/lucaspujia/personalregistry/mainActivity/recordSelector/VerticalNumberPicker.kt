package com.lucaspujia.personalregistry.mainActivity.recordSelector

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.RECORD_MAX_VALUE
import com.lucaspujia.personalregistry.mainActivity.RECORD_MIN_VALUE
import com.lucaspujia.personalregistry.mainActivity.RECORD_PIXELS_PER_UNIT
import com.lucaspujia.personalregistry.mainActivity.RECORD_SCROLL_INVERTED
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.DecimalVisualTransformation
import com.lucaspujia.personalregistry.utils.rememberNumberFormatter
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sign

// TODO: simplificar parametros
@Composable
fun VerticalNumberPicker(
    value: Double,
    onValueChange: (Double) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
    precision: Int = 1,
    pixelsPerUnit: Float = RECORD_PIXELS_PER_UNIT,
    isScrollInverted: Boolean = RECORD_SCROLL_INVERTED,
    minValue: Double = RECORD_MIN_VALUE,
    maxValue: Double = RECORD_MAX_VALUE,
    label: String = "",
    isSmall: Boolean = false,
    isFocused: Boolean = false,
    onFocused: () -> Unit = {},
) {
    val step = remember(precision) { (10.0).pow(-precision) }
    val scope = rememberCoroutineScope()

    // Animatable solo acepta floats
    val continuousValue = remember { Animatable(value.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    var startDraggingValue by remember { mutableFloatStateOf(0f) }
    var totalDragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(value) {
        if (!isDragging && abs(continuousValue.value - value) > 0.0001) {
            continuousValue.animateTo(
                targetValue = value.toFloat(),
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }
    }

    val draggableState = rememberDraggableState { delta ->
        val directionMultiplier = if (isScrollInverted) 1f else -1f
        totalDragOffset += delta * directionMultiplier
        
        val offsetUnits = totalDragOffset / pixelsPerUnit
        val absOffsetUnits = abs(offsetUnits)
        val valueSensitivity = 1f + (abs(startDraggingValue) / 10000f)

        val result = when {
            absOffsetUnits < 1f -> absOffsetUnits // Tramo 1: Lineal y lento (precisión máxima)
            absOffsetUnits < 2f -> absOffsetUnits * 1.5f // Tramo 1bis: Lineal y rápido
            absOffsetUnits < 3f -> {
                val quadraticPart = (absOffsetUnits - 1f).pow(1.5f) * valueSensitivity
                absOffsetUnits + quadraticPart // Tramo 2: Cuadrático + Sensibilidad por valor
            }
            else -> {
                val quadraticPart = (absOffsetUnits - 1f).pow(1.5f) * valueSensitivity
                val exponentialPart = (absOffsetUnits - 2f).pow(3.0f) * valueSensitivity
                absOffsetUnits + quadraticPart + exponentialPart // Tramo 3: Exponencial
            }
        }
        val acceleratedUnits = result * sign(offsetUnits)
        
        val newValue = (startDraggingValue + acceleratedUnits).toDouble().coerceIn(minValue, maxValue)
        
        scope.launch {
            continuousValue.snapTo(newValue.toFloat())
        }
        
        val factor = (10.0).pow(precision)
        val snapped = ((newValue * factor).roundToLong() / factor)
        if (snapped != value) {
            onValueChange(snapped)
        }
    }

    var valueTextWidthPx by remember { mutableIntStateOf(500) }
    val density = LocalDensity.current
    val locale = LocalConfiguration.current.locales[0]
    val formatter = rememberNumberFormatter(precision, locale)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(
                elevation = if (isFocused) 6.dp else 0.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .background(MaterialTheme.colorScheme.surface)
            .clip(MaterialTheme.shapes.medium)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = { 
                    isDragging = true
                    startDraggingValue = continuousValue.value
                    totalDragOffset = 0f
                    onFocused()
                },
                onDragStopped = {
                    isDragging = false
                    scope.launch {
                        continuousValue.animateTo(
                            targetValue = value.toFloat(),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
            ),
    ) {
        val mainRecordHeight = if (isSmall) 32.dp else 45.dp
        val baseOffset = if (isSmall) 4.dp else 8.dp
        val unitOffsetX = remember(valueTextWidthPx, density) { (valueTextWidthPx / (2f * density.density)).dp + baseOffset }

        val centerIndexFloat = (continuousValue.value / step).toFloat()
        val centerIndexInt = (continuousValue.value / step).roundToLong().toInt()
        val startIndex = centerIndexInt - 2
        val endIndex = centerIndexInt + 2

        val currentTextValue = formatter.format(value)
        val baseFontSize = if (isSmall) MaterialTheme.typography.displaySmall.fontSize else MaterialTheme.typography.displayLarge.fontSize
        val adjustedFontSize = if (currentTextValue.length > 6) {
            baseFontSize * (6.5f / currentTextValue.length).coerceIn(0.4f, 1f)
        } else {
            baseFontSize
        }

        for (i in startIndex..endIndex) {
            val itemValue = i * step
            if (itemValue < minValue - 0.0001 || itemValue > maxValue + 0.0001) continue

            val distanceFromCenter = centerIndexFloat - i
            val absDistance = abs(distanceFromCenter)
            val isMainValue = i == centerIndexInt

            val scale = (1f - absDistance * 0.5f).coerceAtLeast(0.5f)
            val alpha = (1f - (absDistance * 0.3f)).coerceIn(0f, 1f)
            val yOffset = distanceFromCenter * mainRecordHeight.value

            Text(
                text = formatter.format(itemValue),
                fontSize = adjustedFontSize,
                fontWeight = if (isMainValue) FontWeight.Bold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                onTextLayout = { 
                    if (isMainValue && valueTextWidthPx != it.size.width) {
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
            text = unit,
            style = if (isSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.Center)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(0, 0) {
                        placeable.place(
                            x = unitOffsetX.roundToPx(),
                            y = baseOffset.roundToPx() - placeable.height / 2
                        )
                    }
                }
        )

        val showDialog = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(mainRecordHeight + 16.dp)
                .width(unitOffsetX + 64.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onFocused()
                    showDialog.value = true
                }
        )

        ValueInputModal(showDialog, value, precision, unit, label, onValueChange, minValue, maxValue, isSmall)
    }
}

@Composable
private fun ValueInputModal(
    showDialog: MutableState<Boolean>,
    value: Double,
    precision: Int,
    unit: String,
    label: String,
    onValueChange: (Double) -> Unit,
    minValue: Double,
    maxValue: Double,
    isSmall: Boolean
) {
    if (showDialog.value) {
        val configuration = LocalConfiguration.current
        val symbols = remember(configuration) { DecimalFormatSymbols(configuration.locales[0]) }
        val focusRequester = remember { FocusRequester() }

        // Estado interno simple: siempre usa punto decimal y sin separadores de miles
        var rawText by remember { 
            mutableStateOf(String.format(Locale.US, "%.${precision}f", value))
        }
        var successfulEdit by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        val onConfirm = {
            val parsed = rawText.toDoubleOrNull()
            if (parsed != null) {
                onValueChange(parsed.coerceIn(minValue, maxValue))
                showDialog.value = false
            } else {
                successfulEdit = false
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            text = {
                Column {
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { input ->
                            // Aceptamos el separador del locale pero lo guardamos como punto internamente
                            val text = input.replace(symbols.decimalSeparator, '.')
                            val filtered = text.filter { it.isDigit() || it == '.' }
                            
                            if (filtered.isEmpty() || filtered.toDoubleOrNull() != null || filtered == ".") {
                                rawText = filtered
                                successfulEdit = true
                            }
                        },
                        visualTransformation = remember(symbols) { DecimalVisualTransformation(symbols) },
                        label = { Text("$label ($unit)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                        singleLine = true,
                        textStyle = if (isSmall) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        isError = !successfulEdit,
                        colors = OutlinedTextFieldDefaults.colors(errorBorderColor = MaterialTheme.colorScheme.error)
                    )
                    if (!successfulEdit) {
                        Text(
                            text = stringResource(R.string.invalid_value),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) { Text(stringResource(R.string.accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@ThemePreviews
@Composable
fun VerticalNumberPickerPreview() {
    var value by remember { mutableDoubleStateOf(75.0) }
    Row(modifier = Modifier.fillMaxWidth()) {
        VerticalNumberPicker(
            value = value,
            onValueChange = { },
            unit = "kg",
            label = "Weight",
            modifier = Modifier.weight(1f).height(120.dp)
        )
    }
}