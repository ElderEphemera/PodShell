package com.elderephemera.podshell.ui

import androidx.compose.runtime.Composable

interface AppTab {
    val title : String

    @Composable
    fun FabIcon()
    fun fabOnClick()

    @Composable
    fun AdditionalContent() {}
}