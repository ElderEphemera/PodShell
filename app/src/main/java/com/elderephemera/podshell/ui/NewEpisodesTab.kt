package com.elderephemera.podshell.ui

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NewEpisodesTab : AppTab {
    override val title = "NEW EPISODES"

    override fun listItems(): Flow<List<ListItemCard>> = flowOf(listOf())

    @Composable
    override fun Fab() = FloatingActionButton(
        onClick = {},
        backgroundColor = MaterialTheme.colors.primary,
        content = { Icon(Icons.Filled.Star, contentDescription = "") },
    )
}