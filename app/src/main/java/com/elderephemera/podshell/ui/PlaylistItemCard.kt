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
import com.elderephemera.podshell.DownloadsSingleton
import com.elderephemera.podshell.PodDownloadService
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaylistItemCard(
    private val feed: Feed,
    private val episode: Episode,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val player: Player,
    private val episodesRepository: EpisodesRepository,
) : ListItemCard {
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
        scope.launch {
            episodesRepository.updateEpisode(episode.copy(inPlaylist = false))
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "Deleted ${episode.title}",
                actionLabel = "Undo",
            )
            if (result == SnackbarResult.ActionPerformed) {
                episodesRepository.updateEpisode(episode.copy(inPlaylist = true))
            } else {
                DownloadsSingleton.getInstance()?.cacheDataSourceFactory?.cache
                    ?.removeResource(episode.guid)
            }
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
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.constrainAs(topText) {
                centerHorizontallyTo(parent)
                top.linkTo(parent.top, 3.dp)
            }
        )
        Text(
            text = episode.pubDateDisplay,
            style = MaterialTheme.typography.caption,
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
        val context = LocalContext.current

        val downloadRequest: DownloadRequest =
            DownloadRequest.Builder(episode.guid, Uri.parse(episode.source))
                .setCustomCacheKey(episode.guid)
                .build()
        val download: Download? = DownloadsSingleton.getInstance(context)
            .downloadManager
            .downloadIndex
            .getDownload(downloadRequest.id)

        if (download?.state == Download.STATE_DOWNLOADING) {
            if (download.percentDownloaded > 0) {
                CircularProgressIndicator(
                    download.percentDownloaded/100,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .3f),
                    modifier = Modifier.size(abProgressSize)
                )
            } else {
                CircularProgressIndicator(
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .3f),
                    modifier = Modifier.size(abProgressSize)
                )
            }
            Icon(
                Icons.Filled.Download,
                contentDescription = "Downloading",
                tint = MaterialTheme.colors.onPrimary.copy(alpha = .5f),
                modifier = Modifier.size(abIconSize)
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
                    modifier = Modifier.size(abProgressSize)
                )
            } else if (episode.position != null && episode.length != null) {
                CircularProgressIndicator(
                    episode.position.toFloat()/episode.length,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .25f),
                    modifier = Modifier.size(abProgressSize)
                )
            }

            if (player.isPlaying && player.currentMediaItem?.mediaId == episode.guid) {
                IconButton(onClick = player::pause) {
                    Icon(
                        Icons.Filled.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(abIconSize)
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
                        val position =
                            if (episode.position != null &&
                                episode.length != null &&
                                episode.position < episode.length) episode.position
                            else 0
                        player.setMediaItem(downloadMediaItem.buildUpon()
                            .setMediaMetadata(metadata)
                            .build(), position)
                        player.prepare()
                    }
                    player.play()
                }) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(abIconSize)
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
                    modifier = Modifier.size(abIconSize)
                )
            }
        }
    }
}