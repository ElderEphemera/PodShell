package com.elderephemera.podshell.ui

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.elderephemera.podshell.RefreshWorker
import com.elderephemera.podshell.data.EpisodesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
        val scope = rememberCoroutineScope()
        val episodes by episodesRepository.getAllNewEpisodes().collectAsState(initial = listOf())
        val noEpisodes by remember { derivedStateOf { episodes.isEmpty() } }
        if (noEpisodes) {
            FloatingActionButton(
                onClick = { scope.launch { RefreshWorker.runRefresh(context) } },
                backgroundColor = MaterialTheme.colors.primary,
                content = { Icon(Icons.Filled.Refresh, contentDescription = "Refresh feeds") },
            )
        } else {
            FloatingActionButton(
                onClick = { scope.launch { episodesRepository.clearNewEpisodes() } },
                backgroundColor = MaterialTheme.colors.primary,
                content = { Icon(Icons.Filled.ClearAll, contentDescription = "Clear new") },
            )
        }
    }
}