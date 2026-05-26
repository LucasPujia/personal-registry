package com.lucaspujia.personalregistry.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun pressedInteractionSource(callback: () -> Unit): MutableInteractionSource {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(500)
            while (true) {
                callback()
                delay(100)
            }
        }
    }

    return interactionSource
}