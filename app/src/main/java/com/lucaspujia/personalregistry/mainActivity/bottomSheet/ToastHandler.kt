package com.lucaspujia.personalregistry.mainActivity.bottomSheet

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ToastHandler(toasts: List<RegistryToast>) {
    if (toasts.isEmpty()) return

    val maxVisible = 3
    // Obtenemos los 3 más antiguos para procesarlos primero (FIFO)
    val oldestToasts = remember(toasts) { toasts.takeLast(maxVisible) }
    // Los mostramos invertidos para que el más reciente de ese grupo esté arriba
    val visibleToasts = remember(oldestToasts) { oldestToasts.reversed() }
    val enqueuedCount = toasts.size - oldestToasts.size

    Popup(
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                horizontalAlignment = Alignment.CenterHorizontally,
                reverseLayout = false,
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
                    // Un toast es "nuevo/encolado" si no era parte de los 3 que estarían visibles
                    // si los mensajes se procesaran estrictamente en orden de llegada.
                    // Usamos la posición en la lista original para determinarlo.
                    val indexInOriginal = toasts.indexOf(toast)
                    val wasEnqueued = indexInOriginal < toasts.size - maxVisible

                    ToastItem(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(300),
                            fadeOutSpec = tween(300),
                            placementSpec = tween(300)
                        ),
                        toast = toast,
                        wasEnqueued = wasEnqueued
                    )
                }
            }
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
    modifier: Modifier = Modifier,
    toast: RegistryToast,
    wasEnqueued: Boolean
) {
    val actions = LocalMainActivityActions.current
    
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            actions.dismissToast(toast.id)
        }
    }

    LaunchedEffect(toast.id) {
        val duration = if (wasEnqueued) 3000L else 4000L
        delay(duration.milliseconds)
        actions.dismissToast(toast.id)
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        modifier = modifier
    ) {
        Surface(
            color = toast.containerColor(MaterialTheme.colorScheme),
            contentColor = toast.contentColor(MaterialTheme.colorScheme),
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
