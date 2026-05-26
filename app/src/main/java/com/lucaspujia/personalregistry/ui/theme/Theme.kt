package com.lucaspujia.personalregistry.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lucaspujia.personalregistry.mainActivity.settings.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    background = BackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray,
    secondary = HighlightOrange,
    tertiary = CardBackground,
    outlineVariant = Color.LightGray.copy(alpha = 0.3f),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceVariant = SurfaceVariantDark,
    secondary = HighlightOrangeDark,
    tertiary = CardBackgroundDark,
    outlineVariant = Color.Gray.copy(alpha = 0.3f),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

@Immutable
data class ExtendedColors(
    val trendIncrease: Color,
    val trendDecrease: Color,
    val trendNeutral: Color
)

val lightExtendedColors = ExtendedColors(
    trendIncrease = TrendIncrease,
    trendDecrease = TrendDecrease,
    trendNeutral = TrendNeutral
)

val darkExtendedColors = ExtendedColors(
    trendIncrease = TrendIncreaseDark,
    trendDecrease = TrendDecreaseDark,
    trendNeutral = TrendNeutralDark
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        trendIncrease = Color.Unspecified,
        trendDecrease = Color.Unspecified,
        trendNeutral = Color.Unspecified
    )
}

@Suppress("UnusedReceiverParameter")
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun PersonalRegistryTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Solo controlamos la apariencia de los iconos (claro/oscuro)
            // La transparencia la maneja enableEdgeToEdge() en la Activity
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
