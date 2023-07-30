package com.elderephemera.podshell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PlayerControls(player: Player) = Column(
    modifier = Modifier
        .background(MaterialTheme.colors.surface)
) {
    val iconSize = 35.dp

    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(C.TIME_UNSET) }
    var hasMediaItem by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(key1 = Unit) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                currentPosition = player.currentPosition
                duration = player.duration
                hasMediaItem = player.currentMediaItem != null
                isPlaying = player.isPlaying
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    Timeline(currentPosition, duration, hasMediaItem, player::seekTo)
    Row(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 5.dp)) {
        Text(currentPosition.milliseconds.formatHMS, color = MaterialTheme.colors.onSurface)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier.weight(1f, fill = true)
        ) {
            IconButton(onClick = { player.seekBack() }, enabled = hasMediaItem) {
                Icon(
                    Icons.Filled.FastRewind,
                    contentDescription = "Skip Back",
                    modifier = Modifier.size(iconSize)
                )
            }
            if (isPlaying) {
                IconButton(onClick = { player.pause() }, enabled = hasMediaItem) {
                    Icon(
                        Icons.Filled.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(iconSize)
                    )
                }
            } else {
                IconButton(onClick = { player.play() }, enabled = hasMediaItem) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
            IconButton(onClick = { player.seekForward() }, enabled = hasMediaItem) {
                Icon(
                    Icons.Filled.FastForward,
                    contentDescription = "Skip Forward",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        Text(duration.milliseconds.formatHMS, color = MaterialTheme.colors.onSurface)
    }
}

@Composable
fun Timeline(
    currentPosition: Long,
    duration: Long,
    hasMediaItem: Boolean,
    seekTo: (Long) -> Unit,
) {
    var changingValue: Float? by remember { mutableStateOf(null) }

    Slider(
        value = changingValue ?: (currentPosition.toFloat() / duration),
        onValueChange = { changingValue = it },
        onValueChangeFinished = {
            changingValue?.let { seekTo((it * duration).toLong()) }
            changingValue = null
        },
        enabled = hasMediaItem,
        modifier = Modifier.padding(10.dp, 0.dp)
    )
}

val Duration.formatHMS: String get() = toComponents { hours, minutes, seconds, _ ->
    if (this.isNegative()) "0:00"
    else if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}