package com.codeprep.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.codeprep.app.MainActivity
import com.codeprep.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodePrepNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                HEARTS_CHANNEL_ID,
                context.getString(R.string.notification_channel_hearts_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_hearts_description)
            }
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                STREAK_CHANNEL_ID,
                context.getString(R.string.notification_channel_streak_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_streak_description)
            }
        )
    }

    fun showHeartsRefilledNotification() {
        showNotification(
            channelId = HEARTS_CHANNEL_ID,
            notificationId = HEARTS_NOTIFICATION_ID,
            title = context.getString(R.string.notification_hearts_title),
            body = context.getString(R.string.notification_hearts_body)
        )
    }

    fun showStreakReminderNotification() {
        showNotification(
            channelId = STREAK_CHANNEL_ID,
            notificationId = STREAK_NOTIFICATION_ID,
            title = context.getString(R.string.notification_streak_title),
            body = context.getString(R.string.notification_streak_body)
        )
    }

    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        body: String
    ) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(createLaunchPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createLaunchPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val HEARTS_NOTIFICATION_ID = 1001
        private const val STREAK_NOTIFICATION_ID = 1002

        const val HEARTS_CHANNEL_ID = "codeprep_hearts"
        const val STREAK_CHANNEL_ID = "codeprep_streak"
    }
}
