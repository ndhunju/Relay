package com.ndhunju.relay.service

import android.telephony.SmsManager

interface MessagingService {

    /**
     * Sends [message] to [address].
     * @param address: It's a phone number like +14083207000
     * @param message: It's should be a non empty string
     */
    fun sendMessage(address: String, message: String)

}

/**
 * Implements [MessagingService] with [SmsManager]
 */
class SmsMessagingService(
    private val smsManager: SmsManager
): MessagingService {

    override fun sendMessage(address: String, message: String) {
        smsManager.sendTextMessage(address, null, message, null, null)
    }
}

class OfflineFirstMessagingService: MessagingService {

    override fun sendMessage(address: String, message: String) {
        // 1. Store the message in database
        // 2. The UI should automatically update based on the database changes with sync status pending
        // 3. Make the async call to send the message
        // 4. Success -> update the sync status
        // 5. Failure -> update the sync status and show option to retry
    }
}