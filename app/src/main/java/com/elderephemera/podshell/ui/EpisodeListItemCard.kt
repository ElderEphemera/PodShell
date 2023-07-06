package com.elderephemera.podshell.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import kotlinx.coroutines.launch

class EpisodeListItemCard(
    private val episode: Episode,
    private val episodesRepository: EpisodesRepository,
) : ListItemCard {
    override val showLogo = false
    @Composable
    override fun Logo() {}

    override val title = episode.title
    override val url = episode.url
    override val subtitle = "Published " + episode.pubDate
    override val description = episode.description

    @Composable
    override fun ActionButton() {
        val coroutineScope = rememberCoroutineScope()
        IconButton(
            onClick = {
                coroutineScope.launch {
                    episodesRepository.updateEpisode(episode.copy(inPlaylist = !episode.inPlaylist))
                }
            },
        ) {
            if (episode.inPlaylist) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete episode")
            } else {
                Icon(Icons.Filled.Add, contentDescription = "Add episode")
            }
        }
    }
}