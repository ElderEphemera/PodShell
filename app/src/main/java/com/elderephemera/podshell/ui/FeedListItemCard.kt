package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.Feed
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class FeedListItemCard(
    private val feed: Feed,
    private val openList: () -> Unit,
    private val unsubscribe: () -> Unit,
) : ListItemCard {
    override val key = feed.id

    override val showLogo = true
    @Composable
    override fun Logo() = AsyncImage(
        model = feed.logo,
        contentDescription = feed.title,
        contentScale = ContentScale.FillWidth,
    )

    override fun onLongClick(coroutineScope: CoroutineScope) = unsubscribe()

    override val title = feed.title
    override val url = feed.url
    override val subtitle = (Instant.now().epochSecond - feed.refreshed).seconds.toComponents {
        days, hours, minutes, _, _ ->
        if (days == 1L) "Refreshed 1 day ago"
        else if (days != 0L) "Refreshed $days days ago"
        else if (hours == 1) "Refreshed 1 hour ago"
        else if (hours != 0) "Refreshed $hours hours ago"
        else if (minutes == 1) "Refreshed 1 minute ago"
        else if (minutes != 0) "Refreshed $minutes minutes ago"
        else "Refreshed just now"
    }
    override val description = feed.description

    @Composable
    override fun ActionButton() = IconButton(onClick = openList) {
        Icon(
            Icons.Filled.List,
            contentDescription = "Show feed episodes",
            modifier = Modifier.size(abIconSize)
        )
    }
}