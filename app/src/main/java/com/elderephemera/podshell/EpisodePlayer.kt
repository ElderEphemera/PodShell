package com.elderephemera.podshell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerNotificationManager
import coil.imageLoader
import coil.request.ImageRequest
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask

@OptIn(UnstableApi::class)
class EpisodePlayer private constructor(
    underlyingPlayer: Player
) : Player by underlyingPlayer {
    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "player"
    }

    constructor(context: Context, episodesRepository: EpisodesRepository) : this(
        underlyingPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context).setDataSourceFactory(
                    DownloadsSingleton.getInstance(context).cacheDataSourceFactory
                )
            )
            .build()
    ) {
        addListener(UpdateTimePlayerListener(this, episodesRepository))
        createNotificationChannel(context)
        setupNotificationManager(context)
    }

    private class UpdateTimePlayerListener(
        private val player: Player,
        private val episodesRepository: EpisodesRepository,
    ) : Player.Listener {
        private val timer = Timer()
        private var timerTask: TimerTask? = null
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                timerTask = timerTask {
                    CoroutineScope(Dispatchers.Main).launch {
                        player.currentMediaItem?.mediaId?.let {
                            episodesRepository
                                .updateEpisodeTime(it, player.currentPosition, player.duration)
                        }
                    }
                }
                timer.scheduleAtFixedRate(timerTask, 0, 1000)
            } else {
                timerTask?.cancel()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Player"
            val descriptionText = "Episode player display and controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupNotificationManager(
        context: Context,
    ) = PlayerNotificationManager
        .Builder(context, NOTIFICATION_ID, CHANNEL_ID)
        .setMediaDescriptionAdapter(EpisodeMediaDescriptionAdapter(context, this))
        .build().apply {
            setPlayer(this@EpisodePlayer)
            setUseChronometer(true)
            setUsePreviousAction(false)
        }

    private class EpisodeMediaDescriptionAdapter(
        private val context: Context,
        private val episodePlayer: EpisodePlayer,
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence =
            episodePlayer.currentEpisode?.title ?: "Podshell"

        override fun createCurrentContentIntent(player: Player): PendingIntent? = null

        override fun getCurrentContentText(player: Player): CharSequence? = null

        private var recentEpisodeGuid: String? = null
        private var recentEpisodeLogo: Bitmap? = null
        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            episodePlayer.currentEpisode?.let { episode ->
                if (recentEpisodeGuid == episode.guid && recentEpisodeLogo != null) {
                    return recentEpisodeLogo
                } else {
                    val request = ImageRequest.Builder(context)
                        .data(episode.logo)
                        .target(onSuccess = { result ->
                            if (result is BitmapDrawable) {
                                recentEpisodeGuid = episode.guid
                                recentEpisodeLogo = result.bitmap
                                callback.onBitmap(result.bitmap)
                            }
                        })
                        .build()
                    context.imageLoader.enqueue(request)
                }
            }
            return null
        }
    }

    var currentEpisode: Episode? = null
        private set

    fun setEpisode(episode: Episode, mediaItem: MediaItem) {
        currentEpisode = episode
        setMediaItem(mediaItem)
    }
}