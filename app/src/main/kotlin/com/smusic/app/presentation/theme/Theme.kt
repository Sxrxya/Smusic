package com.smusic.app.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SMusicDarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = AccentPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = AccentPrimary,
    secondary = AccentSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = AccentSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = AccentSecondary,
    tertiary = AccentTeal,
    onTertiary = TextPrimary,
    tertiaryContainer = AccentTeal.copy(alpha = 0.2f),
    onTertiaryContainer = AccentTeal,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = BorderDivider,
    outlineVariant = TextMuted,
    error = Error,
    onError = TextPrimary,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error,
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundPrimary,
    inversePrimary = AccentPrimary,
    scrim = BackgroundPrimary.copy(alpha = 0.7f),
)

@Composable
fun SMusicTheme(content: @Composable () -> Unit) {
    val colorScheme = SMusicDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundPrimary.toArgb()
            window.navigationBarColor = BackgroundPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SMusicTypography,
        shapes = SMusicShapes,
        content = content
    )
}
