package com.ndhunju.relay.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ndhunju.relay.BuildConfig
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.MainActivity
import com.ndhunju.relay.util.worker.UploadNewMessagesWorker
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all the [Notification] that this app shows
 */
@Singleton
class NotificationManager @Inject constructor(
   val context: Context
) {

    private val notificationManager by lazy {
        val notificationManagerCompat  = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerCompat.createNotificationChannels(getNotificationChannels())
        }
        notificationManagerCompat
    }

    /**
     * [PendingIntent] that opens [MainActivity]
     */
    private val defaultPendingIntent by lazy {
        val intent = Intent(context,  MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannels() = listOf(
        NotificationChannel(
            CHANNEL_ID_CRITICAL_MSG,
            context.getString(R.string.channel_name_critical_notification),
            NotificationManager.IMPORTANCE_HIGH
        ),
        NotificationChannel(
            CHANNEL_ID_UPLOAD_NEW_MSG,
            context.getString(R.string.channel_name_message_upload_status),
            NotificationManager.IMPORTANCE_DEFAULT
        ),
        NotificationChannel(
            CHANNEL_ID_NEW_MSG_FROM_CHILD,
            context.getString(R.string.channel_name_new_message_notification),
            NotificationManager.IMPORTANCE_HIGH
        )

    )

    private val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    /**
     * [Notification.Builder] that sets default values
     */
    private fun defaultNotificationBuilder(channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(defaultPendingIntent)
            .setSound(defaultSoundUri)
    }

    /**
     * [Notification] to be shown when [UploadNewMessagesWorker] is running
     */
    fun getNotificationForUploadingNewMessages(): Notification {
        return defaultNotificationBuilder(CHANNEL_ID_UPLOAD_NEW_MSG)
            .setContentText(context.getString(R.string.notification_uploading_new_messages))
            .setProgress(100, 20, true)
            .setGroup(GROUP_ID_UPLOAD_NEW_MSG)
            .setOngoing(true)
            .build()
    }

    /**
     * Returns [Notification] to be shown when a new message from a child
     * is received.
     */
    fun getNotificationForNewMessageFromChild(msg: String): Notification {
        return defaultNotificationBuilder(CHANNEL_ID_NEW_MSG_FROM_CHILD)
            .setAutoCancel(true)
            .setContentText(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setGroup(GROUP_ID_NEW_MSG_FROM_CHILD)
            .build()
    }

    /**
     * Returns [Notification] to a show a critical message
     */
    fun getNotificationForCriticalMessage(msg: String): Notification {
        return defaultNotificationBuilder(CHANNEL_ID_CRITICAL_MSG)
            .setAutoCancel(true)
            .setContentText(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_ID_CRITICAL_MSG)
            .build()
    }

    /**
     * Notifies user about [Notification]
     */
    fun notify(id: String?, notification: Notification) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(id?.hashCode() ?: 0, notification)
        }
    }

    fun notifyCriticalMessage(msg: String) {
        notify(System.currentTimeMillis().toString(), getNotificationForCriticalMessage(msg))
    }

    companion object {
        // Must be unique per package
        const val CHANNEL_ID_UPLOAD_NEW_MSG = BuildConfig.APPLICATION_ID  + "CHANNEL_ID_UPLOAD_NEW_MSG"
        const val CHANNEL_ID_NEW_MSG_FROM_CHILD = BuildConfig.APPLICATION_ID  + "CHANNEL_ID_NEW_MSG_FROM_CHILD"
        const val CHANNEL_ID_CRITICAL_MSG = BuildConfig.APPLICATION_ID  + "CHANNEL_ID_CRITICAL_MSG"

        const val ID_UPLOAD_NEW_MESSAGES = 1

        const val GROUP_ID_UPLOAD_NEW_MSG = "GROUP_ID_UPLOAD_NEW_MSG"
        const val GROUP_ID_NEW_MSG_FROM_CHILD = "GROUP_ID_NEW_MSG_FROM_CHILD"
        const val GROUP_ID_CRITICAL_MSG = "GROUP_ID_CRITICAL_MSG"
    }

}