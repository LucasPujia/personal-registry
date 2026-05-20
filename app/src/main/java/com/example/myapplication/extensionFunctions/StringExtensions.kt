package com.example.myapplication.extensionFunctions

fun String.capitalize(): String {
    return this.replaceFirstChar { it.titlecase()}
}

fun String.isFloat(): Boolean {
    return this.isEmpty()
            || (this.all { char -> char.isDigit() || char == '.' || char == ',' } && this.count { char -> char == '.' || char == ',' } <= 1)
}