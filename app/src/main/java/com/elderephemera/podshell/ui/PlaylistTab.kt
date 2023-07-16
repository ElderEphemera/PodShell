package com.elderephemera.podshell.ui

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.runtime.*
import androidx.media3.common.Player
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.FeedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlaylistTab(
    private val feedsRepository: FeedsRepository,
    private val episodesRepository: EpisodesRepository,
    private val snackbarHostState: SnackbarHostState,
    private val player: Player,
) : AppTab {
    override val title = "PLAYLIST"

    private val sortingOrders = SortingOrder.values()
    private var sortingOrder by mutableStateOf(SortingOrder.DATE_OLDEST)

    override fun listItems(): Flow<List<ListItemCard>> =
        episodesRepository.getAllEpisodesInPlaylist().map {
            it.sortedWith(sortingOrder.comparator).map { episode ->
                val feed = feedsRepository.getFeed(episode.feedId)
                PlaylistItemCard(feed, episode, player, episodesRepository)
            }
        }

    @Composable
    override fun Fab() {
        val coroutineScope = rememberCoroutineScope()
        FloatingActionButton(
            onClick = {
                sortingOrder = sortingOrders[(sortingOrder.ordinal+1) % sortingOrders.size]
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(sortingOrder.message)
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            content = { Icon(Icons.Filled.SortByAlpha, contentDescription = "Change sort method") },
        )
    }

    private enum class SortingOrder(val display: String, val comparator: Comparator<Episode>) {
        DATE_OLDEST(display = "date (oldest first)", compareBy { it.pubDateTime }),
        DATE_NEWEST(display = "date (newest first)", DATE_OLDEST.comparator.reversed()),
        FEED(display = "feed", compareBy { it.feedId }),
        TITLE(display = "title", compareBy { it.title }),
        ;

        val message = "Sorted by $display"
    }
}