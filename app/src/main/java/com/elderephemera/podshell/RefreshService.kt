package com.elderephemera.podshell

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elderephemera.podshell.data.AppDataContainer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class RefreshService : Service() {
    companion object {
        const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "refresh"

        private const val WORKER_TAG = "refresh"

        fun cancelNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }

        fun ensureRefreshScheduled(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val workInfosFuture = workManager.getWorkInfosByTag(WORKER_TAG)
            workInfosFuture.addListener(
                {
                    if (workInfosFuture.get().isEmpty()) {
                        scheduleRefresh(context, workManager)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }

        fun rescheduleRefresh(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(WORKER_TAG)
            scheduleRefresh(context, workManager)
        }

        private fun scheduleRefresh(context: Context, workManager: WorkManager) {
            MainScope().launch {
                val interval = context.prefAutoRefreshInterval.flow.first().hours
                workManager.enqueue(
                    PeriodicWorkRequestBuilder<RefreshWorker>(interval.toJavaDuration())
                        .addTag(WORKER_TAG)
                        .build()
                )
            }
        }

        class RefreshWorker(private val context: Context, workerParams: WorkerParameters)
            : ListenableWorker(context, workerParams) {
            override fun startWork(): ListenableFuture<Result> {
                val intent = Intent(context, RefreshService::class.java)
                ContextCompat.startForegroundService(context, intent)
                return Futures.immediateFuture(Result.success())
            }
        }
    }

    private val job = SupervisorJob()

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int = synchronized(this) {
        val appContainer = AppDataContainer(this)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        ensureNotificationChannel(
            channelId = CHANNEL_ID,
            name = "Refresh",
            descriptionText = "Status of refreshing feeds",
            importance = NotificationManager.IMPORTANCE_LOW,
        )
        startForeground(
            NOTIFICATION_ID,
            createProgressNotification("feeds", total = 0, completed = 0)
        )

        CoroutineScope(Dispatchers.IO + job).launch {
            val feeds = appContainer.feedsRepository.getAllFeeds().first()
            feeds.forEachIndexed { idx, feed ->
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createProgressNotification(feed.title, total = feeds.size, completed = idx)
                )
                appContainer.feedsRepository.updateFeed(feed.id, feed.rss, markNew = true)
            }

            val new = appContainer.episodesRepository.getAllNewEpisodes().first()
            if (new.isEmpty()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                notificationManager.notify(NOTIFICATION_ID, createFinishedNotification(new.size))
                stopForeground(STOP_FOREGROUND_DETACH)
            }

            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createProgressNotification(feedTitle: String, total: Int, completed: Int) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Refreshing $feedTitle...")
            .setSmallIcon(R.drawable.ic_notification)
            .setProgress(total, completed, false)
            .build()

    private fun createFinishedNotification(newCount: Int) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("New episodes: $newCount")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(mainActivityPendingIntent(tab = 1))
            .build()

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?) = null
}