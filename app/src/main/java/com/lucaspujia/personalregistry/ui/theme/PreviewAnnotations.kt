package com.lucaspujia.personalregistry.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

// === BASIC ===
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class DarkPreview

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
annotation class LightPreview

@LightPreview
@DarkPreview
annotation class ThemePreviews

// === DIALOGS ===
@Preview(
    name = "Light for Dialog",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    widthDp = 500,
    heightDp = 500
)
annotation class LightPreviewForDialog

@Preview(
    name = "Dark for Dialog",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 500,
    heightDp = 500
)
annotation class DarkPreviewForDialog

@LightPreviewForDialog
@DarkPreviewForDialog
annotation class DialogPreviews

// === WITH UI ===
@Preview(
    name = "Dark with System UI",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showSystemUi = true
)
annotation class DarkPreviewWithSystemUI

@Preview(
    name = "Light with System UI",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
annotation class LightPreviewWithSystemUI