package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import kotlinx.coroutines.launch

@Composable
fun EmojiChipsSelector(
    emojiState: EmojiState,
    setEmojiState: (EmojiState) -> Unit,
    focusRequester: FocusRequester
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip( // ICON CHIP
            selected = !emojiState.isEmojiMode,
            onClick = {
                if (emojiState.isEmojiMode) {
                    setEmojiState(emojiState.copy(isEmojiMode = false, showIconSelector = true))
                }
            },
            label = { Text(stringResource(R.string.use_icon)) },
            leadingIcon = if (!emojiState.isEmojiMode) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        FilterChip( // EMOJI CHIP
            selected = emojiState.isEmojiMode,
            onClick = {
                if (!emojiState.isEmojiMode) {
                    setEmojiState(emojiState.copy(isEmojiMode = true, showIconSelector = false))
                    coroutineScope.launch {
                        // Pequeño delay para asegurar que el campo es editable antes de pedir el foco
                        focusRequester.requestFocus()
                    }
                }
            },
            label = { Text(stringResource(R.string.use_emoji)) },
            leadingIcon = if (emojiState.isEmojiMode) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
    }
}