package com.ndhunju.relay.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.getStringForColumn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelayRepository @Inject constructor(private val context: Context) {

    /**
     * Reads the SMS from the device and returns it as a list of [Message]
     */
    fun getLastSmsBySender(): List<Message> {
        val uri = Uri.parse("content://sms")
        val cursor: Cursor? = context.contentResolver.query(
            uri,
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

    private val smsColumns = arrayOf(
        "thread_id", "status", "type", "subject", "person",
        "reply_path_present", "address","body", "date"
    )

    private fun fromCursor(cursor: Cursor): Message {
        return Message(
            cursor.getStringForColumn("thread_id"),
            cursor.getStringForColumn("address"),
            cursor.getStringForColumn("body"),
            cursor.getStringForColumn("date"),
            cursor.getStringForColumn("type"),
            cursor.getStringForColumn("type")
        )
    }

}