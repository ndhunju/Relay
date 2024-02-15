package com.ndhunju.relay.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.analyticsprovider.d
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RelayFirebaseMessagingService: FirebaseMessagingService() {

    private val appComponent by lazy {
        (applicationContext as RelayApplication).appComponent
    }

    private val notificationManager by lazy {
        appComponent.notificationManager()
    }

    private val apiInterface by lazy {
        appComponent.apiInterface()
    }

    private val analyticsManager by lazy {
        appComponent.analyticsManager()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        (applicationContext as RelayApplication).applicationScope.launch(Dispatchers.IO) {
            // A new token is created, register it in the server
            apiInterface.postUserPushNotificationToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            analyticsManager.d(TAG, "Message data payload is not empty")

            // Check if data needs to be processed by long running job
            if (needsToBeScheduled()) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                // Check if message contains a notification payload.
                remoteMessage.notification?.body?.let { body ->
                    analyticsManager.d(TAG, "Message Notification Body is not null")
                    notificationManager.notify(
                        remoteMessage.messageId,
                        notificationManager.getNotificationForNewMessageFromChild(body)
                    )
                }

            }
        }
    }

    /**
     * Need to implement.
     * Returns true if processing or showing notification would take more than 10 seconds
     */
    private fun needsToBeScheduled() = false

    private fun scheduleJob() {}

    companion object {
        val TAG: String = RelayFirebaseMessagingService::class.java.simpleName
    }


}