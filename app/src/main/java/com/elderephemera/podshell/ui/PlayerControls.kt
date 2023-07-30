package com.elderephemera.podshell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PlayerControls(player: Player) = Column(
    modifier = Modifier
        .background(MaterialTheme.colors.surface)
) {
    var currentPosition by remember { mutableStateOf(player.currentPosition) }
    var duration by remember { mutableStateOf(player.duration) }
    var hasMediaItem by remember { mutableStateOf(player.currentMediaItem != null) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }

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

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 5.dp)
    ) {
        Timestamp(currentPosition, align = TextAlign.Left)
        PlayerButtons(
            player::seekBack, player::pause, player::play, player::seekForward,
            stop = player::clearMediaItems,
            hasMediaItem, isPlaying,
        )
        Timestamp(duration, align = TextAlign.Right)
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

@Composable
fun Timestamp(timeMs: Long, align: TextAlign) = Text(
    timeMs.milliseconds.toComponents { hours, minutes, seconds, _ ->
        if (timeMs == C.TIME_UNSET) "0:00"
        else if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, seconds)
        else "%d:%02d".format(minutes, seconds)
    },
    color = MaterialTheme.colors.onSurface,
    textAlign = align,
    modifier = Modifier.fillMaxWidth()
)

@Composable
fun PlayerButtons(
    seekBack: () -> Unit,
    pause: () -> Unit,
    play: () -> Unit,
    seekForward: () -> Unit,
    stop: () -> Unit,
    hasMediaItem: Boolean,
    isPlaying: Boolean,
) = Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
) {
    val iconSize = 35.dp

    IconButton(onClick = preferencesDialog()) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = "Preferences",
            modifier = Modifier.size(iconSize)
        )
    }
    IconButton(onClick = seekBack, enabled = hasMediaItem) {
        Icon(
            Icons.Filled.FastRewind,
            contentDescription = "Skip Back",
            modifier = Modifier.size(iconSize)
        )
    }
    if (isPlaying) {
        IconButton(onClick = pause, enabled = hasMediaItem) {
            Icon(
                Icons.Filled.Pause,
                contentDescription = "Pause",
                modifier = Modifier.size(iconSize)
            )
        }
    } else {
        IconButton(onClick = play, enabled = hasMediaItem) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier.size(iconSize)
            )
        }
    }
    IconButton(onClick = seekForward, enabled = hasMediaItem) {
        Icon(
            Icons.Filled.FastForward,
            contentDescription = "Skip Forward",
            modifier = Modifier.size(iconSize)
        )
    }
    IconButton(onClick = stop, enabled = hasMediaItem) {
        Icon(
            Icons.Filled.Stop,
            contentDescription = "Stop",
            modifier = Modifier.size(iconSize)
        )
    }
}