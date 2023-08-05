package com.elderephemera.podshell.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

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