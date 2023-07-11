package com.elderephemera.podshell

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
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

        private var instance: EpisodePlayer? = null
        fun getInstance(context: Context, episodesRepository: EpisodesRepository) =
            instance ?: EpisodePlayer(
                underlyingPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(
                        DefaultMediaSourceFactory(context).setDataSourceFactory(
                            DownloadsSingleton.getInstance(context).cacheDataSourceFactory
                        )
                    )
                    .build()
            ).apply {
                addListener(updateTimePlayerListener(episodesRepository))
                startPlayerService(context)
                createNotificationChannel(context)
                setupNotificationManager(context)
                instance = this
            }
    }

    private fun updateTimePlayerListener(episodesRepository: EpisodesRepository) =
        object : Player.Listener {
            private val timer = Timer()
            private var timerTask: TimerTask? = null
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    timerTask = timerTask {
                        CoroutineScope(Dispatchers.Main).launch {
                            currentMediaItem?.mediaId?.let {
                                episodesRepository.updateEpisodeTime(it, currentPosition, duration)
                            }
                        }
                    }
                    timer.scheduleAtFixedRate(timerTask, 0, 1000)
                } else {
                    timerTask?.cancel()
                }
            }
        }

    private var playerServiceBinder: PlayerService.Binder? = null

    private fun startPlayerService(context: Context) {
        if (playerServiceBinder == null) {
            val intent = Intent(context, PlayerService::class.java)
            context.startService(intent)
            context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
                    playerServiceBinder = binder as PlayerService.Binder
                }

                override fun onServiceDisconnected(component: ComponentName?) {
                    playerServiceBinder = null
                }
            }, 0)
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
        .setMediaDescriptionAdapter(mediaDescriptionAdapter(context))
        .setNotificationListener(notificationListener)
        .build().apply {
            setPlayer(this@EpisodePlayer)
            setUseChronometer(true)
            setUsePreviousAction(false)
        }

    private fun mediaDescriptionAdapter(context: Context) =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence =
                currentEpisode?.title ?: "Podshell"

            override fun createCurrentContentIntent(player: Player): PendingIntent? =
                TaskStackBuilder.create(context).run {
                    addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
                    getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                }

            override fun getCurrentContentText(player: Player): CharSequence? = null

            private var recentEpisodeGuid: String? = null
            private var recentEpisodeLogo: Bitmap? = null
            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                currentEpisode?.let { episode ->
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

    private val notificationListener =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    playerServiceBinder?.service?.startForeground(notificationId, notification)
                } else {
                    playerServiceBinder?.service?.stopForeground(Service.STOP_FOREGROUND_DETACH)
                }
            }
        }

    var currentEpisode: Episode? = null
        private set

    fun setEpisode(episode: Episode, mediaItem: MediaItem) {
        currentEpisode = episode
        setMediaItem(mediaItem)
    }
}