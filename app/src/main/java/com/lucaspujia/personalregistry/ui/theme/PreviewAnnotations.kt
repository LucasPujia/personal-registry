package com.lucaspujia.personalregistry.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

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