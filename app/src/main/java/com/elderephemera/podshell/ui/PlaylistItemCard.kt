package com.elderephemera.podshell.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.Feed

class PlaylistItemCard(
    private val feed: Feed,
    private val episode: Episode,
) : ListItemCard {
    override val showLogo = true
    @Composable
    override fun Logo() = AsyncImage(
        model = episode.logo ?: feed.logo,
        contentDescription = episode.title,
        contentScale = ContentScale.FillWidth,
    )

    override val title = episode.title
    override val url = episode.url
    override val subtitle = feed.title
    override val description = episode.description

    @Composable
    override fun ActionButton() = IconButton(onClick = {}) {
        Icon(Icons.Filled.Download, contentDescription = "Download episode")
    }
}