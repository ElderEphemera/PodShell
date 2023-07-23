package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EpisodeListItemCard(
    private val episode: Episode,
    private val episodesRepository: EpisodesRepository,
    override val showLogo: Boolean,
) : ListItemCard {
    override val key = episode.id

    @Composable
    override fun Logo() = AsyncImage(
        model = episode.logo,
        contentDescription = episode.title,
        contentScale = ContentScale.FillWidth,
    )

    override val title = episode.title
    override val url = episode.url
    override val subtitle = "Published " + episode.pubDateDisplay
    override val description = episode.description

    @Composable
    override fun ActionButton() {
        val coroutineScope = rememberCoroutineScope()

        if (episode.position != null && episode.length != null) {
            CircularProgressIndicator(
                episode.position.toFloat()/episode.length,
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = .25f),
                modifier = Modifier.size(abProgressSize)
            )
        }

        IconButton(
            onClick = {
                coroutineScope.launch {
                    episodesRepository.updateEpisode(episode.copy(
                        inPlaylist = !episode.inPlaylist,
                        new = false,
                    ))
                }
            },
        ) {
            if (episode.inPlaylist) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete episode",
                    modifier = Modifier.size(abIconSize)
                )
            } else {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add episode",
                    modifier = Modifier.size(abIconSize)
                )
            }
        }
    }

    override fun onLongClick(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            episodesRepository.updateEpisode(episode.copy(new = false))
        }
    }
}