package com.kaius.keyboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CleanDarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = StatusSuccess,
    background = AppBg,
    surface = SurfaceBar,
    surfaceVariant = SurfaceElevated
)

@Composable
fun KaiusKeyboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanDarkColorScheme,
        content = content
    )
}
