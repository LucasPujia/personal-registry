package com.lucaspujia.personalregistry.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Crea y recuerda un DecimalFormat para optimizar la performance en Compose.
 */
@Composable
fun rememberNumberFormatter(precision: Int, locale: Locale = LocalConfiguration.current.locales[0]): DecimalFormat {
    return remember(precision, locale) {
        val symbols = DecimalFormatSymbols(locale)
        val pattern = StringBuilder("#,##0")
        if (precision > 0) {
            pattern.append(".")
            repeat(precision) { pattern.append("0") }
        }
        DecimalFormat(pattern.toString(), symbols)
    }
}

/**
 * Formatea un número Double según la precisión y el locale proporcionados.
 * Incluye separadores de miles y decimales localizados.
 */
fun Double.format(precision: Int, locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols(locale)
    val pattern = StringBuilder("#,##0")
    if (precision > 0) {
        pattern.append(".")
        repeat(precision) { pattern.append("0") }
    }
    val formatter = DecimalFormat(pattern.toString(), symbols)
    return formatter.format(this)
}

/**
 * Transformación visual que añade separadores de miles y decimales según el locale
 * sin modificar el estado interno del TextField.
 */
class DecimalVisualTransformation(private val symbols: DecimalFormatSymbols) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val parts = original.split('.')
        val intPart = parts[0]
        val decPart = if (parts.size > 1) symbols.decimalSeparator + parts[1] else ""

        val formattedInt = StringBuilder()
        for (i in intPart.indices) {
            formattedInt.append(intPart[i])
            val remaining = intPart.length - i - 1
            if (remaining > 0 && remaining % 3 == 0) {
                formattedInt.append(symbols.groupingSeparator)
            }
        }

        val transformed = formattedInt.toString() + decPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var o = 0
                var t = 0
                while (o < offset && o < original.length) {
                    if (t < transformed.length && transformed[t] == symbols.groupingSeparator) {
                        t++
                        continue
                    }
                    o++
                    t++
                }
                return t
            }

            override fun transformedToOriginal(offset: Int): Int {
                var o = 0
                var t = 0
                while (t < offset && t < transformed.length) {
                    if (transformed[t] == symbols.groupingSeparator) {
                        t++
                        continue
                    }
                    o++
                    t++
                }
                return o
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
