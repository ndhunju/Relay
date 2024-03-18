package com.ndhunju.relay.service

import com.ndhunju.relay.ui.messages.Message

interface DeviceSmsReaderService {
    /**
     * Returns a list of last [Message] for each unique thread_id
     */
    fun getLastMessageForEachThread(): List<Message>

    /**
     * Returns list of message for passed [threadId]
     */
    fun getSmsByThreadId(threadId: String): List<Message>

    /**
     * Returns a [Message] that should be unique and only one for
     * passed [address] and [body] combination.
     */
    fun getMessageByAddressAndBody(address: String, body: String): Message

    /**
     * Returns list of [Message] from passed [address]
     */
    @Suppress("unused")
    fun getMessagesByAddress(address: String): List<Message>

    /**
     * Returns list of [Message] that was received after passed [time]
     *
     * Use following code to get messages from past 15 days
     *
     * Calendar.getInstance(Locale.getDefault()).apply {
     *             timeInMillis = System.currentTimeMillis()
     *             add(Calendar.DAY_OF_MONTH, -15)
     *         }.timeInMillis
     */
    fun getMessagesSince(time: Long): List<Message>
}