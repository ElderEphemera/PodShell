package com.elderephemera.podshell.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.FeedInfo

class FeedListItemCard(private val feed: FeedInfo) : ListItemCard {
    @Composable
    override fun Logo() = AsyncImage(
        model = feed.logo,
        contentDescription = feed.title,
        contentScale = ContentScale.FillWidth,
    )

    override val title = feed.title
    override val url = feed.url
    override val subtitle = feed.numEpisodes.toString() + " Episodes"
    override val description = feed.description

    @Composable
    override fun ActionButton() = IconButton(onClick = {}) {
        Icon(Icons.Filled.List, contentDescription = "Show feed episodes")
    }
}