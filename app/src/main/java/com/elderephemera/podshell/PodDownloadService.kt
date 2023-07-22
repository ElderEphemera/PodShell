package com.elderephemera.podshell

import android.app.Notification
import android.app.NotificationManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler

@OptIn(UnstableApi::class)
class PodDownloadService : DownloadService(FOREGROUND_NOTIFICATION_ID) {
    companion object {
        const val JOB_ID = 1
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val CHANNEL_ID = "downloads"
    }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel(
            channelId = CHANNEL_ID,
            name = "Downloads",
            descriptionText = "Ongoing podcast downloads",
            importance = NotificationManager.IMPORTANCE_LOW,
        )
    }

    public override fun getDownloadManager(): DownloadManager =
        DownloadsSingleton.getInstance(this).downloadManager

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification = DownloadNotificationHelper(this, CHANNEL_ID).buildProgressNotification(
        this,
        R.drawable.ic_launcher_foreground,
        null,
        null,
        downloads,
        notMetRequirements,
    )
}