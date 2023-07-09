package com.elderephemera.podshell.ui

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.FeedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NewEpisodesTab(
    private val feedsRepository: FeedsRepository,
    private val episodesRepository: EpisodesRepository,
) : AppTab {
    override val title = "NEW EPISODES"

    override fun listItems(): Flow<List<ListItemCard>> =
        episodesRepository.getAllNewEpisodes().map {
            it.map { episode ->
                EpisodeListItemCard(episode, episodesRepository, showLogo = true)
            }
        }

    @Composable
    override fun Fab() {
        val coroutineScope = rememberCoroutineScope()
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    feedsRepository.getAllFeeds().first().forEach { feed ->
                        feedsRepository.updateFeed(feed.id, feed.rss, markNew = true)
                    }
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            content = { Icon(Icons.Filled.Refresh, contentDescription = "Refresh feeds") },
        )
    }
}