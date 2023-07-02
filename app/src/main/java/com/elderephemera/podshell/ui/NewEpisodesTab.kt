package com.elderephemera.podshell.ui

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable

class NewEpisodesTab : AppTab {
    override val title = "NEW EPISODES"

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Star, contentDescription = "")
    override fun fabOnClick() {}
}