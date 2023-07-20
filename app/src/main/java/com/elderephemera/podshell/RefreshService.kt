package com.elderephemera.podshell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elderephemera.podshell.data.AppDataContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RefreshService : Service() {
    companion object {
        const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "refresh"
    }

    private val job = SupervisorJob()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val feedsRepository = AppDataContainer(this).feedsRepository

        createNotificationChannel()
        val notification = createNotification("feeds", total = 0, completed = 0)
        startForeground(NOTIFICATION_ID, notification)

        CoroutineScope(Dispatchers.IO + job).launch {
            val feeds = feedsRepository.getAllFeeds().first()
            feeds.forEachIndexed { idx, feed ->
                postNotification(feed.title, total = feeds.size, completed = idx)
                feedsRepository.updateFeed(feed.id, feed.rss, markNew = true)
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Refresh"
            val descriptionText = "Status of refreshing feeds"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(feedTitle: String, total: Int, completed: Int) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Refreshing $feedTitle...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(total, completed, false)
            .build()

    private fun postNotification(feedTitle: String, total: Int, completed: Int) {
        val notification = createNotification(feedTitle, total, completed)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?) = null
}