package com.lucaspujia.personalregistry.utils

import net.objecthunter.exp4j.ExpressionBuilder

/**
 * Utilidad para evaluar y validar expresiones matemáticas personalizadas.
 * Utiliza la librería exp4j para el parseo de fórmulas.
 */
object FormulaEvaluator {
    /**
     * Evalúa una fórmula dada utilizando los valores de las dos unidades disponibles.
     * Si la fórmula es nula o inválida, retorna por defecto el valor de la primera unidad.
     *
     * @param formula La expresión matemática (ej: "v1 / v2").
     * @param v1 El valor de la primera unidad.
     * @param v2 El valor de la segunda unidad (opcional).
     * @return El resultado del cálculo matemático.
     */
    fun evaluate(formula: String?, v1: Double, v2: Double?): Double {
        if (formula.isNullOrBlank()) return v1
        return try {
            val expression = ExpressionBuilder(formula)
                .variables("v1", "v2")
                .build()
                .setVariable("v1", v1)
                .setVariable("v2", v2 ?: 0.0)
            expression.evaluate()
        } catch (e: Exception) {
            v1 // En caso de error, retornamos el valor base para evitar crashes
        }
    }

    /**
     * Valida si una cadena de texto es una fórmula matemática válida y parseable.
     *
     * @param formula La cadena a validar.
     * @return True si la fórmula es válida, false de lo contrario.
     */
    fun isValid(formula: String): Boolean {
        if (formula.isBlank()) return false
        return try {
            // Intentamos construir la expresión; si falla, la fórmula no es válida.
            ExpressionBuilder(formula)
                .variables("v1", "v2")
                .build()
            true
        } catch (_: Exception) {
            false
        }
    }
}
