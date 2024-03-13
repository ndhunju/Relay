package com.ndhunju.relay.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
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
        NotificationManagerCompat.from(context)
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

    private val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    /**
     * [Notification.Builder] that sets default values
     */
    private val defaultNotificationBuilder by lazy {
        NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(defaultPendingIntent)
            .setSound(defaultSoundUri)
    }

    /**
     * [Notification] to be shown when [UploadNewMessagesWorker] is running
     */
    fun getNotificationForUploadingNewMessages(): Notification {
        return defaultNotificationBuilder
            .setContentTitle(context.getString(R.string.notification_uploading_new_messages))
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
        return defaultNotificationBuilder
            .setAutoCancel(true)
            .setContentTitle(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setGroup(GROUP_ID_NEW_MSG_FROM_CHILD)
            .build()
    }

    /**
     * Returns [Notification] to a show a critical message
     */
    fun getNotificationForCriticalMessage(msg: String): Notification {
        return defaultNotificationBuilder
            .setAutoCancel(true)
            .setContentTitle(msg)
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
        private const val DEFAULT_CHANNEL_ID = BuildConfig.APPLICATION_ID + "DEFAULT_CHANNEL_ID"
        const val ID_UPLOAD_NEW_MESSAGES = 1

        const val GROUP_ID_UPLOAD_NEW_MSG = "GROUP_ID_UPLOAD_NEW_MSG"
        const val GROUP_ID_NEW_MSG_FROM_CHILD = "GROUP_ID_NEW_MSG_FROM_CHILD"
        const val GROUP_ID_CRITICAL_MSG = "GROUP_ID_CRITICAL_MSG"
    }


}