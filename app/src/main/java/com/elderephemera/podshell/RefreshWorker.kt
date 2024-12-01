package com.elderephemera.podshell

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.elderephemera.podshell.data.AppDataContainer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class RefreshWorker(private val context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {
    companion object {
        private const val REFRESH_NOTIFICATION_ID = 2
        private const val REFRESH_CHANNEL_ID = "refresh"
        private const val NEW_EP_NOTIFICATION_ID = 3
        private const val NEW_EP_CHANNEL_ID = "new-episodes"
        private const val WORKER_TAG = "refresh"

        fun cancelNewEpisodeNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NEW_EP_NOTIFICATION_ID)
        }

        fun ensureRefreshScheduled(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val workInfosFuture = workManager.getWorkInfosByTag(WORKER_TAG)
            workInfosFuture.addListener(
                {
                    val infos = workInfosFuture.get()
                    if (infos.isEmpty() || infos.any { it.state == WorkInfo.State.FAILED }) {
                        scheduleRefresh(context, workManager)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }

        fun scheduleRefresh(context: Context) =
            scheduleRefresh(context, WorkManager.getInstance(context))

        private fun scheduleRefresh(context: Context, workManager: WorkManager) {
            MainScope().launch {
                val interval = context.prefAutoRefreshInterval.flow.first().hours
                workManager.enqueueUniquePeriodicWork(
                    WORKER_TAG,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    PeriodicWorkRequestBuilder<RefreshWorker>(interval.toJavaDuration())
                        .addTag(WORKER_TAG)
                        .build()
                )
            }
        }

        private fun progressNotificationBuilder(context: Context) =
            NotificationCompat.Builder(context, REFRESH_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)

        private fun progressNotification(
            builder: NotificationCompat.Builder,
            feedTitle: String,
            total: Int, completed: Int
        ) = builder
            .setContentTitle("Refreshed $feedTitle...")
            .setProgress(total, completed, false)
            .build()

        private fun createFinishedNotification(
            context: Context, newCount: Int
        ) = NotificationCompat.Builder(context, NEW_EP_CHANNEL_ID)
            .setContentTitle("New episodes: $newCount")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(context.mainActivityPendingIntent(tab = 1))
            .build()

        suspend fun runRefresh(context: Context) = withContext(Dispatchers.IO) {
            Log.i("refresh", "starting refresh")

            val appContainer = AppDataContainer(context)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            context.ensureNotificationChannel(
                channelId = REFRESH_CHANNEL_ID,
                name = "Feed Refresh",
                descriptionText = "Status of refreshing feeds",
                importance = NotificationManager.IMPORTANCE_LOW,
            )
            val notificationBuilder = progressNotificationBuilder(context)

            val feeds = appContainer.feedsRepository.getAllFeeds().first()
            var completed = 0
            feeds.map { feed ->
                async {
                    appContainer.feedsRepository.updateFeed(feed.id, feed.rss, markNew = true)
                    notificationManager.notify(
                        REFRESH_NOTIFICATION_ID,
                        progressNotification(
                            notificationBuilder,
                            feedTitle = feed.title,
                            total = feeds.size,
                            completed = ++completed
                        )
                    )
                }
            }.awaitAll()

            notificationManager.cancel(REFRESH_NOTIFICATION_ID)

            val new = appContainer.episodesRepository.getAllNewEpisodes().first()
            if (!new.isEmpty()) {
                context.ensureNotificationChannel(
                    channelId = NEW_EP_CHANNEL_ID,
                    name = "New Episodes",
                    descriptionText = "New episode alerts",
                    importance = NotificationManager.IMPORTANCE_DEFAULT,
                )
                notificationManager.notify(
                    NEW_EP_NOTIFICATION_ID,
                    createFinishedNotification(context, new.size)
                )
            }

            Log.i("refresh", "refresh finished")
        }
    }

    override suspend fun doWork(): Result {
        runRefresh(context)
        return Result.success()
    }
}