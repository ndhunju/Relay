package com.ndhunju.relay.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ndhunju.relay.RelayApplication

class RelayFirebaseMessagingService: FirebaseMessagingService() {

    private val notificationManager by lazy {
        (applicationContext as RelayApplication).appComponent.notificationManager()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // A new token is created, register it in the server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val msg = remoteMessage.data["data"]
        if (msg != null) {
            notificationManager.notify(
                remoteMessage.messageId,
                notificationManager.getNotificationForNewMessageFromChild(msg)
            )
        }
    }


}