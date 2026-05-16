package com.example.myapplication.extensionFunctions

fun String.capitalize(): String {
    return this.replaceFirstChar { it.titlecase()}
}