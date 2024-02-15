package com.ndhunju.relayserver

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.io.FileInputStream
import java.util.ArrayList

import java.util.Arrays





object RelayServer {

    /**
     * See https://firebase.google.com/docs/cloud-messaging/send-message?authuser=0
     * for sample codes
     */

    init {
        val refreshToken = FileInputStream("service-account-file.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(refreshToken))
            //.setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
            .build()

        FirebaseApp.initializeApp(options)
    }

    fun sendPushNotification(registrationToken: String, msg: String) {
        // This registration token comes from the client FCM SDKs.


        // See documentation on defining a message payload.
        val message: Message = Message.builder()
            .putData("msg", msg)
            .setToken(registrationToken)
            .build()


        // Send a message to the device corresponding to the provided
        // registration token.
        val response = FirebaseMessaging.getInstance().send(message)
        // Response is a message ID string.
        println("Successfully sent message: $response")

    }

    fun sendPushNotifications(registrationTokens: List<String>, msg: String) {
        // These registration tokens come from the client FCM SDKs.

        val message: MulticastMessage = MulticastMessage.builder()
            .putData("score", "850")
            .putData("time", "2:45")
            .addAllTokens(registrationTokens)
            .build()

        val response: BatchResponse = FirebaseMessaging.getInstance().sendMulticast(message)
        if (response.getFailureCount() > 0) {
            val responses: List<SendResponse> = response.getResponses()
            val failedTokens: MutableList<String> = java.util.ArrayList<String>()
            for (i in responses.indices) {
                if (!responses[i].isSuccessful()) {
                    // The order of responses corresponds to the order of the registration tokens.
                    failedTokens.add(registrationTokens[i])
                }
            }
            println("List of tokens that caused failures: $failedTokens")
        }
    }
}