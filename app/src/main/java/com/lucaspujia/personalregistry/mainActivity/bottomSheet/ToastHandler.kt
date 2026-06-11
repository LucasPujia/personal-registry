package com.lucaspujia.personalregistry.mainActivity.bottomSheet

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.RegistryToast
import com.lucaspujia.personalregistry.ui.theme.DialogPreviews
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.extendedColors
import com.lucaspujia.personalregistry.utils.mockMainActivityViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ToastHandler(toasts: List<RegistryToast>) {
    if (toasts.isEmpty()) return

    Popup(
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        ToastListContent(toasts = toasts)
    }
}

@Composable
private fun ToastListContent(toasts: List<RegistryToast>) {
    val maxVisible = 3
    // Obtenemos los 3 más antiguos (al final de la lista [newest...oldest])
    // takeLast(3) preserva el orden -> [T_newest_of_3, T_middle, T_oldest]
    val visibleToasts = remember(toasts) { toasts.takeLast(maxVisible) }
    val enqueuedCount = toasts.size - visibleToasts.size

    LazyColumn(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = false
    ) {
        if (enqueuedCount > 0) {
            item(key = "hint") {
                ToastHint(
                    count = enqueuedCount,
                    modifier = Modifier.animateItem()
                )
            }
        }

        items(visibleToasts, key = { it.id }) { toast ->
            ToastItem(
                toast = toast,
                shortDuration = toasts.size > maxVisible,
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(300),
                    fadeOutSpec = tween(300),
                    placementSpec = tween(300)
                )
            )
        }
    }
}

@Composable
private fun ToastHint(
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Text(
            text = stringResource(id = R.string.more_toasts, count),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ToastItem(
    toast: RegistryToast,
    shortDuration: Boolean,
    modifier: Modifier = Modifier
) {
    val actions = LocalMainActivityActions.current
    
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.51f }
    )

    // Solo se descarta cuando se suelta y superó el umbral
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            delay(150.milliseconds)
            actions.dismissToast(toast.id)
        }
    }

    LaunchedEffect(toast.id) {
        val duration = if (shortDuration) 3000L else 4000L
        delay(duration.milliseconds)
        actions.dismissToast(toast.id)
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            color = toast.containerColor(MaterialTheme.colorScheme, MaterialTheme.extendedColors),
            contentColor = toast.contentColor(MaterialTheme.colorScheme, MaterialTheme.extendedColors),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(imageVector = toast.icon, contentDescription = null)
                Text(
                    text = stringResource(id = toast.textRes),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@DialogPreviews
@Composable
fun ToastHandlerPreview() {
    val toasts = listOf(
        RegistryToast.Success(R.string.record_added_success),
        RegistryToast.Error(R.string.record_added_success),
        RegistryToast.Error(R.string.generic_error),
    )
    PersonalRegistryTheme(mainActivityViewModel = mockMainActivityViewModel()) {
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
            ToastListContent(toasts = toasts)
        }
    }
}
