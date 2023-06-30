package com.elderephemera.podshell.ui

import androidx.compose.runtime.Composable

interface ListItemCard {
    @Composable
    fun Logo()

    val title: String
    val url: String
    val subtitle: String
    val description: String

    @Composable
    fun ActionButton()
}