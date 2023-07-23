package com.elderephemera.podshell.ui

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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

    override val key = episode.id

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
    override fun ActionButton() = ConstraintLayout {
        val (topText, centerBox, bottomText) = createRefs()

        ActionButtonCenter(
            modifier = Modifier.constrainAs(centerBox) {}.fillMaxSize()
        )
        Text(
            text = episode.lengthDisplay,
            fontSize = 13.xp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.constrainAs(topText) {
                centerHorizontallyTo(parent)
                top.linkTo(parent.top, 3.dp)
            }
        )
        Text(
            text = episode.pubDateDisplay,
            fontSize = 13.xp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.constrainAs(bottomText) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom, 3.dp)
            }
        )
    }

    @Composable
    private fun ActionButtonCenter(modifier: Modifier) = Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val iconSize = 35.dp
        val progressSize = 45.dp

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
                    modifier = Modifier.size(progressSize)
                )
            } else {
                CircularProgressIndicator(
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .3f),
                    modifier = Modifier.size(progressSize)
                )
            }
            Icon(
                Icons.Filled.Download,
                contentDescription = "Downloading",
                tint = MaterialTheme.colors.onPrimary.copy(alpha = .5f),
                modifier = Modifier.size(iconSize)
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
                    modifier = Modifier.size(progressSize)
                )
            } else if (episode.position != null && episode.length != null) {
                CircularProgressIndicator(
                    episode.position.toFloat()/episode.length,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .25f),
                    modifier = Modifier.size(progressSize)
                )
            }

            if (player.isPlaying && player.currentMediaItem?.mediaId == episode.guid) {
                IconButton(onClick = player::pause) {
                    Icon(
                        Icons.Filled.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(iconSize)
                    )
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
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(iconSize)
                    )
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
                Icon(
                    Icons.Filled.Download,
                    contentDescription = "Download",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}