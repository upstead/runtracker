package com.upstead.runtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue500,
    onPrimary = White,
    secondary = Mint500,
    onSecondary = White,
    tertiary = Coral500,
    background = Cloud100,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    outline = Slate300
)

private val DarkColors = darkColorScheme(
    primary = Blue500,
    secondary = Mint500,
    tertiary = Coral500
)

@Composable
fun RunTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
