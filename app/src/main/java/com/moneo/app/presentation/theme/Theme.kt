package com.moneo.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MoneoLightColorScheme = lightColorScheme(
    primary = MoneoBlack,
    onPrimary = MoneoWhite,
    primaryContainer = MoneoSilver,
    onPrimaryContainer = MoneoNearBlack,
    secondary = MoneoCharcoal,
    onSecondary = MoneoWhite,
    secondaryContainer = MoneoOffWhite,
    onSecondaryContainer = MoneoCharcoal,
    tertiary = MoneoAccent,
    onTertiary = MoneoWhite,
    background = MoneoWhite,
    onBackground = MoneoBlack,
    surface = MoneoWhite,
    onSurface = MoneoBlack,
    surfaceVariant = MoneoOffWhite,
    onSurfaceVariant = MoneoDarkGray,
    outline = MoneoSilver,
    outlineVariant = MoneoLightGray,
    error = MoneoRed,
    onError = MoneoWhite,
)

private val MoneoDarkColorScheme = darkColorScheme(
    primary = MoneoWhite,
    onPrimary = MoneoBlack,
    primaryContainer = MoneoCharcoal,
    onPrimaryContainer = MoneoOffWhite,
    secondary = MoneoLightGray,
    onSecondary = MoneoNearBlack,
    secondaryContainer = MoneoDarkSurfaceVariant,
    onSecondaryContainer = MoneoSilver,
    tertiary = MoneoAccent,
    onTertiary = MoneoWhite,
    background = MoneoDarkSurface,
    onBackground = MoneoWhite,
    surface = MoneoDarkSurface,
    onSurface = MoneoWhite,
    surfaceVariant = MoneoDarkSurfaceVariant,
    onSurfaceVariant = MoneoLightGray,
    outline = MoneoCharcoal,
    outlineVariant = MoneoDarkSurfaceElevated,
    error = MoneoRedDark,
    onError = MoneoBlack,
)

@Composable
fun MoneoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MoneoDarkColorScheme else MoneoLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MoneoTypography,
        shapes = MoneoShapes,
        content = content
    )
}
