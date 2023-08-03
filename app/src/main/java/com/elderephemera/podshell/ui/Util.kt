package com.elderephemera.podshell.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderephemera.podshell.prefOverrideTextSize

val Int.xp: TextUnit @Composable get() {
    val overrideTextSize by LocalContext.current.prefOverrideTextSize.state()
    return if (overrideTextSize) with(LocalDensity.current) { this@xp.dp.toSp() } else this.sp
}

val abIconSize = 35.dp
val abProgressSize = 45.dp