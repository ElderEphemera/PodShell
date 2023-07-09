package com.elderephemera.podshell.ui

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import coil.compose.AsyncImage
import com.elderephemera.podshell.PodDownloadService
import com.elderephemera.podshell.DownloadsSingleton
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.Feed

@OptIn(UnstableApi::class)
class PlaylistItemCard(
    private val feed: Feed,
    private val episode: Episode,
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

        var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }

        if (download?.state == Download.STATE_DOWNLOADING) {
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
            IconButton(onClick = {
                if (exoPlayer == null) {
                    ExoPlayer.Builder(context)
                        .setMediaSourceFactory(
                            DefaultMediaSourceFactory(context).setDataSourceFactory(
                                DownloadsSingleton.getInstance().cacheDataSourceFactory
                            )
                        ).build()
                        .apply {
                            setMediaItem(downloadRequest.toMediaItem())
                            prepare()
                            exoPlayer = this
                        }
                }
                if (exoPlayer?.isPlaying == true) {
                    exoPlayer?.pause()
                } else {
                    exoPlayer?.play()
                }
            }) {
                if (exoPlayer?.isPlaying == true) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause")
                } else {
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