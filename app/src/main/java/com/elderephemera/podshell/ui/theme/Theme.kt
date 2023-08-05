package com.elderephemera.podshell.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,
    onPrimary = Color.White,
    background = Color(0xFF1E1A1E),
    surface = Color(0xFF383238),
    error = Color(0xFFE87523),
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun PodShellTheme(
    darkTheme: Boolean,
    overrideTextSize: Boolean,
    content: @Composable () -> Unit,
) = MaterialTheme(
    colors = if (darkTheme) DarkColorPalette else LightColorPalette,
    typography = if (overrideTextSize) Typography.overrideTextSize() else Typography,
    shapes = Shapes,
    content = content
)