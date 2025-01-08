package com.enaboapps.switchify.activities.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color.Red,
    onPrimary = Color.White,
    primaryContainer = Color.Red,
    onPrimaryContainer = Color.White,
    secondary = Color.Red,
    onSecondary = Color.White,
    tertiary = Color.Red
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Red,
    onPrimary = Color.White,
    primaryContainer = Color.Red,
    onPrimaryContainer = Color.White,
    secondary = Color.Red,
    onSecondary = Color.White,
    tertiary = Color.Red
)

@Composable
fun SwitchifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}