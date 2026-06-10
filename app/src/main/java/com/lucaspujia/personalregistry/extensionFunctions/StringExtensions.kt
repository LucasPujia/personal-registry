package com.lucaspujia.personalregistry.extensionFunctions

fun String.capitalize(): String {
    return this.replaceFirstChar { it.titlecase()}
}

fun String.isDouble(): Boolean = this.isEmpty()
            || (this.all { char -> char.isDigit() || char == '.' || char == ',' }
                && this.count { char -> char == '.' || char == ',' } <= 1)
