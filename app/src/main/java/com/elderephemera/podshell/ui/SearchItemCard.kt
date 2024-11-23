package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.mr3y.podcastindex.model.PodcastFeed

class SearchItemCard(
    private val feed: PodcastFeed,
    private val subscribe: () -> Unit,
): ListItemCard {
    override val key = feed.id
    override val showLogo = true

    @Composable
    override fun Logo() = AsyncImage(
        model = feed.image,
        contentDescription = feed.title,
        contentScale = ContentScale.FillWidth,
    )

    override val title = feed.title
    override val url = feed.url
    override val subtitle = feed.author
    override val description = feed.description

    @Composable
    override fun ActionButton() = IconButton(onClick = subscribe) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = "Show feed episodes",
            modifier = Modifier.size(abIconSize)
        )
    }
}