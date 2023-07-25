package com.elderephemera.podshell

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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

fun Context.mainActivityPendingIntent(tab: Int = 0): PendingIntent {
    val intent = Intent(this, MainActivity::class.java).apply {
        putExtra("tab", tab)
    }
    return TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )!! // "May return null only if PendingIntent.FLAG_NO_CREATE has been supplied"
    }
}

fun vibrateClick(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= 29) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}