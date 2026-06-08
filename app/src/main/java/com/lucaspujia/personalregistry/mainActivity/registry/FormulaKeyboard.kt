package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.utils.FormulaEvaluator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Contenedor animado para el teclado de fórmulas.
 * Maneja la visibilidad, la animación de entrada/salida y el botón físico 'atrás'.
 */
@Composable
fun FormulaKeyboardWrapper(
    visible: Boolean,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClose: () -> Unit,
    unit1Symbol: String,
    unit2Symbol: String,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = visible) { onClose() }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        FormulaKeyboard(
            value = value,
            onValueChange = onValueChange,
            onClose = onClose,
            unit1Symbol = unit1Symbol,
            unit2Symbol = unit2Symbol
        )
    }
}

/**
 * Componente principal del teclado de la calculadora.
 */
@Composable
private fun FormulaKeyboard(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClose: () -> Unit,
    unit1Symbol: String,
    unit2Symbol: String
) {
    val u1Label = unit1Symbol.ifBlank { "v1" }
    val u2Label = unit2Symbol.ifBlank { "v2" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .navigationBarsPadding()
        ) {
            // Visualización de la fórmula y resultado de ejemplo
            FormulaPreview(value.text, u1Label, u2Label, value.selection.start)

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de controles de navegación y acciones rápidas
            ControlRow(value, onValueChange, onClose)

            Spacer(modifier = Modifier.height(8.dp))

            // Rejilla de botones de la calculadora
            KeyboardGrid(value, onValueChange, u1Label, u2Label)
        }
    }
}

@Composable
private fun ControlRow(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Flecha Atrás: Salta bloques de variables (v1, v2)
            ControlKey(Icons.AutoMirrored.Filled.ArrowBack, Modifier.width(72.dp).height(36.dp)) {
                val pos = value.selection.start
                val jump = if (pos >= 2 && value.text.isVarAt(pos - 2)) 2 else 1
                onValueChange(value.copy(selection = TextRange((pos - jump).coerceAtLeast(0))))
            }
            // Flecha Adelante: Salta bloques de variables (v1, v2)
            ControlKey(Icons.AutoMirrored.Filled.ArrowForward, Modifier.width(72.dp).height(36.dp)) {
                val pos = value.selection.start
                val jump = if (pos <= value.text.length - 2 && value.text.isVarAt(pos)) 2 else 1
                onValueChange(value.copy(selection = TextRange((pos + jump).coerceAtMost(value.text.length))))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onValueChange(TextFieldValue("")) }) {
                Text(stringResource(R.string.clear).uppercase(), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.done).uppercase(), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KeyboardGrid(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    u1Label: String,
    u2Label: String
) {
    val buttons = listOf(
        listOf("7", "8", "9", "/", "v1"),
        listOf("4", "5", "6", "*", "v2"),
        listOf("1", "2", "3", "-", "("),
        listOf(".", "0", "^", "+", ")")
    )

    buttons.forEachIndexed { rowIndex, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { char ->
                val isVar = char == "v1" || char == "v2"
                CalcKey(
                    text = when (char) {
                        "v1" -> u1Label
                        "v2" -> u2Label
                        else -> char
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isKeyEnabled(char, value),
                    containerColor = if (isVar) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isVar) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        val newText = StringBuilder(value.text).insert(value.selection.start, char).toString()
                        val newCursor = value.selection.start + char.length
                        onValueChange(TextFieldValue(newText, TextRange(newCursor)))
                    }
                )
            }
            // Agregar botón de borrar al final de la última fila para equilibrio visual
            if (rowIndex == buttons.lastIndex) {
                ControlKey(Icons.AutoMirrored.Filled.Backspace, Modifier.weight(1f)) {
                    if (value.selection.start > 0) {
                        val pos = value.selection.start
                        val toDelete = if (pos >= 2 && value.text.isVarAt(pos - 2)) 2 else 1
                        val newText = value.text.removeRange(pos - toDelete, pos)
                        onValueChange(TextFieldValue(newText, TextRange(pos - toDelete)))
                    }
                }
            }
        }
        if (rowIndex < buttons.lastIndex) Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Previsualización de la fórmula con resaltado de variables y cursor animado.
 */
@Composable
private fun FormulaPreview(formula: String, u1: String, u2: String, cursorIndex: Int) {
    val sampleV1 = 10.0
    val sampleV2 = 5.0
    val result = FormulaEvaluator.evaluate(formula, sampleV1, sampleV2)
    val isValid = FormulaEvaluator.isValid(formula)
    
    // Animación de parpadeo del cursor
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            val annotatedFormula = buildAnnotatedString {
                appendAnnotatedWithVars(formula.substring(0, cursorIndex), u1, u2)
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha), fontWeight = FontWeight.ExtraBold)) {
                    append("|")
                }
                appendAnnotatedWithVars(formula.substring(cursorIndex), u1, u2)
            }
            
            Text(
                text = if (formula.isEmpty()) buildAnnotatedString { append(stringResource(R.string.formula_preview_empty)) } else annotatedFormula,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (formula.isNotEmpty()) {
                Text(
                    text = if (isValid) stringResource(R.string.formula_preview_result, sampleV1, u1, sampleV2, u2, result)
                           else stringResource(R.string.formula_invalid),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Botón genérico de la calculadora con soporte para efecto de sacudida (shake) al estar deshabilitado.
 */
@Composable
private fun CalcKey(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            if (enabled) onClick() else {
                scope.launch {
                    repeat(3) {
                        offsetX.animateTo(4f, tween(40))
                        offsetX.animateTo(-4f, tween(40))
                    }
                    offsetX.animateTo(0f, tween(40))
                }
            }
        },
        modifier = modifier
            .height(54.dp)
            .offset { IntOffset(offsetX.value.roundToInt().dp.roundToPx(), 0) },
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.4f),
            contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)
        )
    ) {
        Text(text, fontSize = 16.sp, maxLines = 1)
    }
}

@Composable
private fun ControlKey(icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, contentDescription = null)
    }
}

/**
 * Lógica para determinar si una tecla debe estar habilitada según el contexto de la fórmula.
 */
private fun isKeyEnabled(key: String, value: TextFieldValue): Boolean {
    val text = value.text
    val pos = value.selection.start
    val prevChar = if (pos > 0) text[pos - 1] else null
    val isAfterVar = pos >= 2 && text.isVarAt(pos - 2)

    return when (key) {
        "+", "*", "/", "^" -> (prevChar?.isDigit() == true || prevChar == ')' || isAfterVar)
        "-" -> true // Signo negativo o resta
        ")" -> text.count { it == '(' } > text.count { it == ')' } && (prevChar?.isDigit() == true || isAfterVar || prevChar == ')')
        "v1", "v2" -> prevChar == null || "+-*/(^".contains(prevChar)
        "." -> {
            val lastNum = text.substring(0, pos).split(Regex("[+\\-*/^()]")).last()
            prevChar?.isDigit() == true && !lastNum.contains('.') && !isAfterVar
        }
        "(", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> !isAfterVar
        else -> true
    }
}

// Funciones de extensión auxiliares para mejorar la legibilidad del código

private fun String.isVarAt(index: Int): Boolean {
    if (index < 0 || index > length - 2) return false
    return substring(index, index + 2).let { it == "v1" || it == "v2" }
}

private fun AnnotatedString.Builder.appendAnnotatedWithVars(text: String, u1: String, u2: String) {
    var i = 0
    while (i < text.length) {
        if (text.isVarAt(i)) {
            val label = if (text.substring(i, i + 2) == "v1") u1 else u2
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { // El color se hereda o se define externamente
                append(label)
            }
            i += 2
        } else {
            append(text[i])
            i++
        }
    }
}
