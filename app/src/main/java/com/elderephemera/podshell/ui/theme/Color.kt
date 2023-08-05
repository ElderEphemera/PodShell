package com.elderephemera.podshell.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkPurple = Color(0xFF3700B3)
private val Teal = Color(0xFF03DAC5)

val DarkColorPalette = darkColors(
    primary = Color(0xFF8E47E5),
    primaryVariant = DarkPurple,
    secondary = Teal,
    onPrimary = Color.White,
    background = Color(0xFF1E1A1E),
    surface = Color(0xFF383238),
    onSurface = Color.White,
    error = Color(0xFFE87523),
)

val LightColorPalette = lightColors(
    primary = Color(0xFF6200EE),
    primaryVariant = DarkPurple,
    secondary = Teal,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color(0xFFF1F4FF),
    onSurface = Color.Black,
    error = Color(0xFFA73114),
)

@Composable
fun linkColor() =
    if (MaterialTheme.colors.isLight) Color(0xFF0F79CF)
    else Color(0xFF64B5F6)