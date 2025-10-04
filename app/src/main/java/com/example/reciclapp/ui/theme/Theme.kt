package com.example.reciclapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenDark,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = Black,
    onSurface = Black,
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    secondary = GreenLight,
    background = Black,
    surface = Black,
    onPrimary = White,
    onSecondary = Black,
    onBackground = White,
    onSurface = White,
)

@Composable
fun ReciclAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
