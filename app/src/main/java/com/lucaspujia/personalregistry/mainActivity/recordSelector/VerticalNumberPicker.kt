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
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.extensionFunctions.isFloat
import com.lucaspujia.personalregistry.mainActivity.RECORD_MAX_VALUE
import com.lucaspujia.personalregistry.mainActivity.RECORD_MIN_VALUE
import com.lucaspujia.personalregistry.mainActivity.RECORD_PIXELS_PER_UNIT
import com.lucaspujia.personalregistry.mainActivity.RECORD_SCROLL_INVERTED
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun VerticalNumberPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
    precision: Int = 1,
    pixelsPerUnit: Float = RECORD_PIXELS_PER_UNIT,
    isScrollInverted: Boolean = RECORD_SCROLL_INVERTED,
    minValue: Float = RECORD_MIN_VALUE,
    maxValue: Float = RECORD_MAX_VALUE,
    label: String = "",
    isSmall: Boolean = false,
) {
    val step = remember(precision) { (10.0).pow(-precision).toFloat() }
    val scope = rememberCoroutineScope()
    
    val continuousValue = remember { Animatable(value) }
    var isDragging by remember { mutableStateOf(false) }

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
        val mainRecordHeight = if (isSmall) 32.dp else 45.dp
        val unitOffsetX = remember(valueTextWidthPx, density) { (valueTextWidthPx / (2f * density.density)).dp + (if (isSmall) 8.dp else 16.dp) }

        val centerIndexFloat = continuousValue.value / step
        val centerIndexInt = centerIndexFloat.roundToInt()
        val startIndex = centerIndexInt - 2
        val endIndex = centerIndexInt + 2

        for (i in startIndex..endIndex) {
            val itemValue = i * step
            if (itemValue < minValue - 0.0001f || itemValue > maxValue + 0.0001f) continue

            val distanceFromCenter = centerIndexFloat - i
            val absDistance = abs(distanceFromCenter)
            val isMainValue = i == centerIndexInt

            val scale = (1f - absDistance * 0.5f).coerceAtLeast(0.5f)
            val alpha = (1f - (absDistance * 0.3f)).coerceIn(0f, 1f)
            val yOffset = distanceFromCenter * mainRecordHeight.value

            Text(
                text = "%.${precision}f".format(itemValue),
                fontSize = if (isSmall) MaterialTheme.typography.displaySmall.fontSize else MaterialTheme.typography.displayLarge.fontSize,
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
                .offset(x = unitOffsetX, y = if (isSmall) 4.dp else 8.dp),
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
                    showDialog.value = true
                }
        )

        ValueInputModal(showDialog, value.toString(), unit, label, onValueChange, minValue, maxValue, isSmall)
    }
}

@Composable
private fun ValueInputModal(
    showDialog: MutableState<Boolean>,
    initialValue: String,
    unit: String,
    label: String,
    onValueChange: (Float) -> Unit,
    minValue: Float,
    maxValue: Float,
    isSmall: Boolean
) {
    if (showDialog.value) {
        var editValue by remember { mutableStateOf(initialValue) }
        var successfulEdit by remember { mutableStateOf(initialValue.isFloat()) }

        val closeDialog = { showDialog.value = false }
        val onConfirm = {
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
                        label = { Text("$label ($unit)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                        singleLine = true,
                        textStyle = if (isSmall) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.fillMaxWidth(),
                        isError = !successfulEdit,
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        )
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
