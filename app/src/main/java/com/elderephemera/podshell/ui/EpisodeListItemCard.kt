package com.elderephemera.podshell.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import com.elderephemera.podshell.data.EpisodeInfo

class EpisodeListItemCard(
    episode: EpisodeInfo,
) : ListItemCard {
    @Composable
    override fun Logo() {}

    override val title = episode.title
    override val url = episode.url
    override val subtitle = "Published " + episode.pubDate
    override val description = episode.description

    @Composable
    override fun ActionButton() = IconButton(onClick = {}) {
        Icon(Icons.Filled.Add, contentDescription = "Add episode")
    }
}