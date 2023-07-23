package com.elderephemera.podshell.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

val Int.xp: TextUnit @Composable get() = with(LocalDensity.current) { this@xp.dp.toSp() }

val abIconSize = 35.dp
val abProgressSize = 45.dp