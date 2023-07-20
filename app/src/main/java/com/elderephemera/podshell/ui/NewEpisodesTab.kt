package com.elderephemera.podshell.ui

import android.content.Intent
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.elderephemera.podshell.RefreshService
import com.elderephemera.podshell.data.EpisodesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewEpisodesTab(
    private val episodesRepository: EpisodesRepository,
) : AppTab {
    override val title = "NEW"

    override fun listItems(): Flow<List<ListItemCard>> =
        episodesRepository.getAllNewEpisodes().map {
            it.map { episode ->
                EpisodeListItemCard(episode, episodesRepository, showLogo = true)
            }
        }

    @Composable
    override fun Fab() {
        val context = LocalContext.current
        FloatingActionButton(
            onClick = {
                val intent = Intent(context, RefreshService::class.java)
                context.startService(intent)
                ContextCompat.startForegroundService(context, intent)
            },
            backgroundColor = MaterialTheme.colors.primary,
            content = { Icon(Icons.Filled.Refresh, contentDescription = "Refresh feeds") },
        )
    }
}