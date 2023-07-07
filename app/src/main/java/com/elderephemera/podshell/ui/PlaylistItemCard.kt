package com.elderephemera.podshell.ui

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.Feed

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
        var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
        IconButton(onClick = {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(episode.source))
            }
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
        }) {
            if (mediaPlayer?.isPlaying == true) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            } else {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
            }
        }
    }
}