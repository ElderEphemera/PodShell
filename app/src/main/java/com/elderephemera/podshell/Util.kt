package com.elderephemera.podshell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.TaskStackBuilder

fun Context.ensureNotificationChannel(
    channelId: String,
    name: String,
    descriptionText: String,
    importance: Int,
) {
    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        notificationManager.getNotificationChannel(channelId) == null
    ) {
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        notificationManager.createNotificationChannel(channel)
    }
}

fun Context.mainActivityPendingIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java)
    return TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )!! // "May return null only if PendingIntent.FLAG_NO_CREATE has been supplied"
    }
}