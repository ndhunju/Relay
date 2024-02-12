package com.ndhunju.relay.service

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.getLongForColumn
import com.ndhunju.relay.util.getStringForColumn
import javax.inject.Inject
import javax.inject.Singleton

private val TAG = DeviceSmsReaderService::class.simpleName

/**
 * This class provides API to read SMS messages stored on the device in different ways.
 */
@Singleton
class DeviceSmsReaderService @Inject constructor(
    private val context: Context,
    private val analyticsManager: AnalyticsManager,
) {

    private val smsUri = Uri.parse("content://sms")

    /**
     * Returns a list of last [Message] for each unique thread_id
     */
    fun getLastMessageForEachThread(): List<Message> {
        try {
            val cursor: Cursor? = context.contentResolver.query(
                smsUri,
                smsColumns,
                null,
                null,
                "date DESC" // Show newest message at the top
            )

            return fromCursorToMapOfThreadToLastMessage(cursor).map { entry -> entry.value }
        } catch (ex: Exception) {
            analyticsManager.logEvent("didFailToGetLastMessage", ex.message)
        }

        return emptyList()
    }

    /**
     * Returns list of message for passed [threadId]
     */
    fun getSmsByThreadId(threadId: String): List<Message> {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            // NOTE: $threadId must be wrapped with single quotation
            "thread_id='$threadId'",
            null,
            null
        )

        return fromCursorToMessageList(cursor)
    }

    /**
     * Returns a [Message] that should be unique and only one for
     * passed [address] and [body] combination.
     */
    fun getMessageByAddressAndBody(address: String, body: String): Message {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            // NOTE: $body and $address must be wrapped with single quotation
            "body='$body' AND address='$address'",
            null,
            null
        )

        val messages = fromCursorToMessageList(cursor)

        if (messages.size > 1) {
            analyticsManager.logEvent("didFindManyMessageForAddressAndBody", )
        }

        return messages.first()
    }

    /**
     * Returns list of [Message] from passed [address]
     */
    @Suppress("unused")
    fun getMessagesByAddress(address: String): List<Message> {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            "address='$address'",
            null,
            null
        )

        return  fromCursorToMessageList(cursor)
    }

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
    fun getMessagesSince(time: Long): List<Message> {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            "date >= ?",
            arrayOf("$time"),
            null
        )

        return  fromCursorToMessageList(cursor)
    }

    /**
     * These are all the columns that SMS database base by OS has
     */
    private val smsColumns = arrayOf(
        "_id", "thread_id", "address", "person",  "date", "protocol", "read", "status", "type",
        "reply_path_present", "subject", "body", "service_center", "locked", "error_code", "seen"
    )

    /**
     * Reads the [cursor] and return the list of [Message]
     */
    private fun fromCursorToMessageList(cursor: Cursor?): List<Message> {
        val messages = mutableListOf<Message>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                messages.add(fromCursorToMessage(cursor))
            } while (cursor.moveToNext())
            cursor.close()
        }

        return messages
    }

    /**
     * Reads the [cursor] and returns a map of thread id to last message in that thread
     */
    private fun fromCursorToMapOfThreadToLastMessage(cursor: Cursor?): Map<String, Message> {
        val messagesByThreads = mutableMapOf<String, Message>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val message = fromCursorToMessage(cursor)
                if (messagesByThreads[message.threadId] == null) {
                    messagesByThreads[message.threadId] = message
                } else {
                    if ((messagesByThreads[message.threadId]?.date ?: 0) < message.date) {
                        messagesByThreads[message.threadId] = message
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        }

        return messagesByThreads
    }

    /**
     * Returns a [Message] from the [cursor]
     */
    private fun fromCursorToMessage(cursor: Cursor) = Message(
        cursor.getStringForColumn("_id"),
        cursor.getStringForColumn("thread_id"),
        cursor.getStringForColumn("address"),
        cursor.getStringForColumn("body"),
        cursor.getLongForColumn("date"),
        cursor.getStringForColumn("type"),
        null,
        null
    )

}