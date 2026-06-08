package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.utils.RegistryIcon
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class EmojiState(
    val emoji: String,
    val icon: String,
    val isEmojiMode: Boolean,
    val showIconSelector: Boolean
) {
    fun getCurrentEmoji(): String {
        return if (isEmojiMode) emoji else icon
    }

    fun isEmojiTooLong(): Boolean {
        return isEmojiMode && emoji.isNotEmpty() && run {
            try { emoji.codePointCount(0, emoji.length) > 1 }
            catch (_: Exception) { false }
        }
    }
}

@Composable
fun EmojiSelectorForm(
    emojiState: EmojiState,
    setEmojiState: (EmojiState) -> Unit,
    focusRequester: FocusRequester,
    isDuplicateEmoji: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }
    OutlinedTextField(
        value = emojiState.getCurrentEmoji(),
        onValueChange = { newValue ->
            if (emojiState.isEmojiMode) {
                val codePointCount = try {
                    newValue.codePointCount(0, newValue.length)
                } catch (_: Exception) {
                    0
                }

                if (codePointCount <= 1) {
                    setEmojiState(emojiState.copy(emoji = newValue))
                } else {
                    // Filtro automático: si pegan algo largo, tomamos solo el primer code point
                    try {
                        val firstCodePoint = newValue.codePointAt(0)
                        setEmojiState(
                            emojiState.copy(
                                emoji = String(Character.toChars(firstCodePoint))
                            )
                        )
                        // Trigger shake animation on automatic clipping
                        coroutineScope.launch {
                            shakeOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.2f, stiffness = 1000f),
                                initialVelocity = 100f
                            )
                        }
                    } catch (_: Exception) {
                        // Fallback
                    }
                }
            }
        },
        readOnly = !emojiState.isEmojiMode,
        label = { Text(stringResource(R.string.registry_icon)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        isError = isDuplicateEmoji || emojiState.isEmojiTooLong(),
        placeholder = if (emojiState.isEmojiMode) {
            { Text(stringResource(R.string.emoji_hint)) }
        } else null,
        supportingText = when {
            isDuplicateEmoji -> {
                { Text(stringResource(R.string.duplicate_icon_error)) }
            }

            emojiState.isEmojiTooLong() -> {
                { Text(stringResource(R.string.emoji_length_error)) }
            }

            emojiState.isEmojiMode -> {
                { Text(stringResource(R.string.emoji_hint)) }
            }

            else -> null
        },
        trailingIcon = if (!emojiState.isEmojiMode) {
            {
                IconButton(onClick = { setEmojiState(emojiState.copy(showIconSelector = true)) }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.select_icon)
                    )
                }
            }
        } else null,
        leadingIcon = {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                RegistryIcon(
                    iconIdentifier = emojiState.getCurrentEmoji(),
                    contentDescription = null
                )
            }
        }
    )
}
