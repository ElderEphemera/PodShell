package com.elderephemera.podshell.ui

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.elderephemera.podshell.EpisodePlayer
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.FeedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistTab(
    private val feedsRepository: FeedsRepository,
    private val episodesRepository: EpisodesRepository,
    private val player: EpisodePlayer,
) : AppTab {
    override val title = "PLAYLIST"

    override fun listItems(): Flow<List<ListItemCard>> =
        episodesRepository.getAllEpisodesInPlaylist().map {
            it.map { episode ->
                val feed = feedsRepository.getFeed(episode.feedId)
                PlaylistItemCard(feed, episode, player)
            }
        }

    @Composable
    override fun Fab() = FloatingActionButton(
        onClick = {},
        backgroundColor = MaterialTheme.colors.primary,
        content = { Icon(Icons.Filled.Star, contentDescription = "") },
    )
}