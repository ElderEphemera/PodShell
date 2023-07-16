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
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import coil.compose.AsyncImage
import com.elderephemera.podshell.PodDownloadService
import com.elderephemera.podshell.DownloadsSingleton
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaylistItemCard(
    private val feed: Feed,
    private val episode: Episode,
    private val player: Player,
    private val episodesRepository: EpisodesRepository,
) : ListItemCard {
    companion object {
        private fun Player.seekToOnReady(positionMs: Long) {
            if (availableCommands.contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
                seekTo(positionMs)
            } else {
                addListener(object : Player.Listener {
                    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                        if (availableCommands.contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
                            seekTo(positionMs)
                            removeListener(this)
                        }
                    }
                })
            }
        }
    }

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
            if (player.currentMediaItem?.mediaId == episode.guid) {
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

            if (player.isPlaying && player.currentMediaItem?.mediaId == episode.guid) {
                IconButton(onClick = player::pause) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause")
                }
            } else {
                IconButton(onClick = {
                    if (player.currentMediaItem?.mediaId != episode.guid) {
                        val downloadMediaItem = downloadRequest.toMediaItem()
                        val metadata = MediaMetadata.Builder()
                            .populate(downloadMediaItem.mediaMetadata)
                            .setTitle(episode.title)
                            .setArtist(feed.title)
                            .setArtworkUri(episode.logo?.let(Uri::parse))
                            .build()
                        player.setMediaItem(downloadMediaItem.buildUpon()
                            .setMediaMetadata(metadata)
                            .build())
                        player.prepare()
                        episode.position?.let {
                            if (episode.length != null && it < episode.length) {
                                player.seekToOnReady(it)
                            } else {
                                player.seekToOnReady(0)
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