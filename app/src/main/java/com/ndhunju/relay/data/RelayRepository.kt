package com.ndhunju.relay.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.ndhunju.relay.service.Result
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.getStringForColumn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelayRepository @Inject constructor(private val context: Context) {

    private val TAG = RelayRepository::class.simpleName
    private val smsUri = Uri.parse("content://sms")

    /**
     * Returns a list of last [Message] for each unique thread_id
     */
    fun getLastMessageForEachThread(): List<Message> {
        // TODO: Nikesh - This is not returning last message sent in the thread but by the user
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            "thread_id IS NOT NULL) GROUP BY (thread_id", //GROUP BY,
            null,
            "date DESC" // Show newest message at the top
        )
        val messages = mutableListOf<Message>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val message = fromCursor(cursor)
                //println("SMS from: $message")
                messages.add(message)
            } while (cursor.moveToNext())
            cursor.close()
        }

        return messages
    }

    /**
     * Returns list of message for passed [threadId]
     */
    fun getSmsByThreadId(threadId: String): List<Message> {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            "thread_id='$threadId'",
            null,
            null
        )

        val messages = mutableListOf<Message>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val message = fromCursor(cursor)
                //println("SMS from: $message")
                messages.add(message)
            } while (cursor.moveToNext())
            cursor.close()
        }

        return messages
    }

    /**
     * Returns a [Message] that should be unique and only one for
     * passed [address] and [body] combination.
     */
    fun getMessageByAddressAndBody(address: String, body: String): Message {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            "body='$body' AND address='$address'",
            null,
            null
        )

        val messages = mutableListOf<Message>()
        // TODO: Encapsulate following logic into a extension function for re usability
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val message = fromCursor(cursor)
                //println("SMS from: $message")
                messages.add(message)
            } while (cursor.moveToNext())
            cursor.close()
        }

        if (messages.size > 1) {
            Log.e(TAG, "getMessageBy: Found multiple messages for passed body and address")
        }

        return messages.first()
    }

    /**
     * Returns list of [Message] from passed [address]
     */
    fun getMessagesByAddress(address: String): List<Message> {
        val cursor: Cursor? = context.contentResolver.query(
            smsUri,
            smsColumns,
            "address='$address'",
            null,
            null
        )

        val messages = mutableListOf<Message>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val message = fromCursor(cursor)
                //println("SMS from: $message")
                messages.add(message)
            } while (cursor.moveToNext())
            cursor.close()
        }

        return messages
    }

    /**
     * These are all the columns that SMS database base by OS has
     */
    private val smsColumns = arrayOf(
        "_id", "thread_id", "address", "person",  "date", "protocol", "read", "status", "type",
        "reply_path_present", "subject", "body", "service_center", "locked", "error_code", "seen"
    )

    private fun fromCursor(cursor: Cursor): Message {
        return Message(
            cursor.getStringForColumn("_id"),
            cursor.getStringForColumn("thread_id"),
            cursor.getStringForColumn("address"),
            cursor.getStringForColumn("body"),
            cursor.getStringForColumn("date"),
            cursor.getStringForColumn("type"),
            Result.Pending,
            null
        )
    }

}