package com.elderephemera.podshell

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommands
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.data.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private lateinit var session: MediaSession

    override fun onCreate() {
        super.onCreate()

        val episodesRepository = AppDataContainer(this).episodesRepository

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    DownloadsSingleton.getInstance(this).cacheDataSourceFactory
                )
            )
            .build()
            .apply { addListener(updateTimePlayerListener(episodesRepository)) }

        session = MediaSession.Builder(this, player)
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
    }

    override fun onDestroy() {
        session.player.release()
        session.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session

    private fun updateTimePlayerListener(episodesRepository: EpisodesRepository) =
        object : Player.Listener {
            private val timer = Timer()
            private var timerTask: TimerTask? = null
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    val mediaId = session.player.currentMediaItem?.mediaId
                    if (mediaId != null) {
                        timerTask = timerTask {
                            CoroutineScope(Dispatchers.Main).launch {
                                episodesRepository.updateEpisodeTime(
                                    mediaId,
                                    session.player.currentPosition,
                                    session.player.duration
                                )
                            }
                        }
                        timer.scheduleAtFixedRate(timerTask, 0, 1000)
                    }
                } else {
                    timerTask?.cancel()
                }
            }
        }
}