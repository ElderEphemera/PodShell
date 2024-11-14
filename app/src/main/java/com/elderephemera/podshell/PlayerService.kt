package com.elderephemera.podshell

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.*
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.data.EpisodesRepository
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.timerTask

class PlayerService : MediaSessionService() {
    companion object {
        @OptIn(UnstableApi::class)
        private val playerCommands = Player.Commands.Builder().addAll(
            Player.COMMAND_PLAY_PAUSE,
            Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
            Player.COMMAND_SET_MEDIA_ITEM,
        ).build()
    }

    private val scope = MainScope()

    private lateinit var session: MediaSession

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val episodesRepository = AppDataContainer(this).episodesRepository

        val seekForwardIncrement = prefSeekForwardIncrement.stateFlow(scope)
        val seekBackIncrement = prefSeekBackIncrement.stateFlow(scope)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    DownloadsSingleton.getInstance(this).cacheDataSourceFactory
                )
            )
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .build()
            .apply { addListener(updateTimePlayerListener(episodesRepository)) }
            .let { object : ExoPlayer by it {
                override fun seekForward() =
                    seekTo(currentPosition + seekForwardIncrement.value)

                override fun seekBack() =
                    seekTo(currentPosition - seekBackIncrement.value)
            }}

        scope.launch {
            prefHandleAudioBecomingNoisy.flow.collect {
                player.setHandleAudioBecomingNoisy(it)
            }
        }

        session = MediaSession.Builder(this, player)
            .setSessionActivity(mainActivityPendingIntent())
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    session.setAvailableCommands(controller, SessionCommands.EMPTY, playerCommands)
                    return super.onConnect(session, controller)
                }
            })
            .build()

        setMediaNotificationProvider(mediaNotificationProvider())
    }

    override fun onDestroy() {
        session.player.release()
        session.release()
        scope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session

    private fun updateTimePlayerListener(episodesRepository: EpisodesRepository) =
        object : UpdateTimePlayerListener(scope) {
            override suspend fun updateTime() {
                val mediaId = session.player.currentMediaItem?.mediaId
                if (mediaId != null) {
                    episodesRepository.updateEpisodeTime(
                        mediaId,
                        session.player.currentPosition,
                        session.player.duration
                    )
                }
            }
        }

    abstract class UpdateTimePlayerListener(private val scope: CoroutineScope) : Player.Listener {
        private val timer = Timer()
        private var timerTask: TimerTask? = null

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                timerTask = timerTask { scope.launch { updateTime() } }
                timer.schedule(timerTask, 0, 1000)
            } else {
                cancel()
            }
        }

        fun cancel() = timerTask?.cancel()

        abstract suspend fun updateTime()
    }

    @OptIn(UnstableApi::class)
    private fun mediaNotificationProvider() =
        object : DefaultMediaNotificationProvider(this) {
            override fun getMediaButtons(
                session: MediaSession,
                playerCommands: Player.Commands,
                customLayout: ImmutableList<CommandButton>,
                showPauseButton: Boolean
            ): ImmutableList<CommandButton> = ImmutableList.of(
                CommandButton.Builder()
                    .setPlayerCommand(Player.COMMAND_SEEK_BACK)
                    .setIconResId(R.drawable.fast_rewind)
                    .setExtras(Bundle().apply {
                        putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 0)
                    })
                    .setDisplayName("Skip Back")
                    .build(),
                CommandButton.Builder()
                    .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                    .setIconResId(
                        if (showPauseButton) R.drawable.pause
                        else R.drawable.play_arrow
                    )
                    .setExtras(Bundle().apply {
                        putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 1)
                    })
                    .setDisplayName(if (showPauseButton) "Pause" else "Play")
                    .build(),
                CommandButton.Builder()
                    .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
                    .setIconResId(R.drawable.fast_forward)
                    .setExtras(Bundle().apply {
                        putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 2)
                    })
                    .setDisplayName("Skip Forward")
                    .build(),
            )
        }
}