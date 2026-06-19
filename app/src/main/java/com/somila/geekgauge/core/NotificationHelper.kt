package com.somila.geekgauge.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.somila.geekgauge.MainActivity
import com.somila.geekgauge.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    companion object {
        const val RECORDING_CHANNEL_ID = "recording_channel"
        const val REPORT_CHANNEL_ID = "report_channel"
        const val RECORDING_NOTIFICATION_ID = 1001
        const val REPORT_NOTIFICATION_ID = 1002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val recordingChannel = NotificationChannel(
                RECORDING_CHANNEL_ID,
                "Recording Session",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shown while a session is being recorded"
                setShowBadge(false)
            }

            val reportChannel = NotificationChannel(
                REPORT_CHANNEL_ID,
                "Report Ready",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when an AI report has been generated"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(recordingChannel)
            notificationManager.createNotificationChannel(reportChannel)
        }
    }

    fun buildRecordingNotification() = NotificationCompat.Builder(
        context, RECORDING_CHANNEL_ID
    )
        .setContentTitle("Session Recording")
        .setContentText("Geek Gauge is recording your session")
        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
        .setOngoing(true)
        .setSilent(true)
        .setForegroundServiceBehavior(
            NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        )
        .build()

    fun showReportReadyNotification(geekName: String, sessionId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "report/$sessionId")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REPORT_CHANNEL_ID)
            .setContentTitle("Report Ready")
            .setContentText("$geekName's evaluation report has been generated")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(REPORT_NOTIFICATION_ID, notification)
    }
}