package com.elderephemera.podshell.ui

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import coil.compose.AsyncImage
import com.elderephemera.podshell.PodDownloadService
import com.elderephemera.podshell.DownloadsSingleton
import com.elderephemera.podshell.EpisodePlayer
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaylistItemCard(
    private val feed: Feed,
    private val episode: Episode,
    private val player: EpisodePlayer,
    private val episodesRepository: EpisodesRepository,
) : ListItemCard {
    override val showLogo = true
    @Composable
    override fun Logo() = AsyncImage(
        model = episode.logo ?: feed.logo,
        contentDescription = episode.title,
        contentScale = ContentScale.FillWidth,
    )

    override val title = episode.title
    override val url = episode.url
    override val subtitle = feed.title
    override val description = episode.description

    override fun onLongClick(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            episodesRepository.updateEpisode(episode.copy(inPlaylist = false))
        }
    }

    @Composable
    override fun ActionButton() {
        val context = LocalContext.current

        val downloadRequest: DownloadRequest =
            DownloadRequest.Builder(episode.guid, Uri.parse(episode.source))
                .setCustomCacheKey(episode.guid)
                .build()
        val download: Download? = DownloadsSingleton.getInstance()
            .downloadManager
            .downloadIndex
            .getDownload(downloadRequest.id)

        if (download?.state == Download.STATE_DOWNLOADING) {
            if (download.percentDownloaded > 0) {
                CircularProgressIndicator(
                    download.percentDownloaded/100,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .3f),
                )
            } else {
                CircularProgressIndicator(
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .3f),
                )
            }
            Icon(
                Icons.Filled.Download,
                contentDescription = "Downloading",
                tint = MaterialTheme.colors.onPrimary.copy(alpha = .5f),
            )
        } else if (
            DownloadsSingleton.getInstance(context)
                .cacheDataSourceFactory
                .cache
                ?.getCachedSpans(episode.guid)?.isEmpty()
            == false
        ) {
            if (player.currentMediaItem == downloadRequest.toMediaItem()) {
                CircularProgressIndicator(
                    player.currentPosition.toFloat()/player.duration,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .25f),
                )
            } else if (episode.position != null && episode.length != null) {
                CircularProgressIndicator(
                    episode.position.toFloat()/episode.length,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .25f),
                )
            }

            if (player.isPlaying && player.currentMediaItem == downloadRequest.toMediaItem()) {
                IconButton(onClick = player::pause) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause")
                }
            } else {
                IconButton(onClick = {
                    if (player.currentMediaItem != downloadRequest.toMediaItem()) {
                        player.setEpisode(episode, downloadRequest.toMediaItem())
                        player.prepare()
                        episode.position?.let {
                            if (episode.length != null && it < episode.length) {
                                player.seekTo(it)
                            } else {
                                player.seekTo(0)
                            }
                        }
                    }
                    player.play()
                }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }
            }
        } else {
            IconButton(onClick = {
                DownloadService.sendAddDownload(
                    context,
                    PodDownloadService::class.java,
                    downloadRequest,
                    false,
                )
            }) {
                Icon(Icons.Filled.Download, contentDescription = "Download")
            }
        }
    }
}