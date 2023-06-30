package com.elderephemera.podshell.ui

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AppTab {
    val title : String

    @Composable
    fun FabIcon()
    fun fabOnClick()

    fun listItems(): Flow<List<ListItemCard>> = flowOf(listOf())

    @Composable
    fun AdditionalContent() {}
}