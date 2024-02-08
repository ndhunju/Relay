package com.ndhunju.relay.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
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

    /**
     * [Notification] to be shown when [UploadNewMessagesWorker] is running
     */
    fun getNotificationForUploadingNewMessages(): Notification {
        val intent = Intent(context,  MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_uploading_new_messages))
            .setProgress(100, 20, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL_ID"
        const val ID_UPLOAD_NEW_MESSAGES = 1
    }


}