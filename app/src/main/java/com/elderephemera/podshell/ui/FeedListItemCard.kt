package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.Feed

class FeedListItemCard(
    private val feed: Feed,
    private val openList: () -> Unit
) : ListItemCard {
    @Composable
    override fun Logo() = AsyncImage(
        model = feed.logo,
        contentDescription = feed.title,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.width(75.dp)
    )

    override val title = feed.title
    override val url = feed.url
    override val subtitle = "TODO"
    override val description = feed.description

    @Composable
    override fun ActionButton() = IconButton(onClick = openList) {
        Icon(Icons.Filled.List, contentDescription = "Show feed episodes")
    }
}