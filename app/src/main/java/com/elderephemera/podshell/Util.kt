package com.elderephemera.podshell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

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