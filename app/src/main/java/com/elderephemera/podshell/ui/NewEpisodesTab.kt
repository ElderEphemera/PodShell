package com.elderephemera.podshell.ui

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NewEpisodesTab : AppTab {
    override val title = "NEW EPISODES"

    override fun listItems(): Flow<List<ListItemCard>> = flowOf(listOf())

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Star, contentDescription = "")
    override fun fabOnClick() {}
}