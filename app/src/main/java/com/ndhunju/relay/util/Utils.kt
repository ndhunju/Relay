package com.ndhunju.relay.util

import android.content.ContentResolver
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.net.Uri
import com.ndhunju.relay.ui.messages.Message
import java.util.Locale

val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

/**
 * Reads the SMS from the device and returns it as a list of [Message]
 */
fun getLastSmsBySender(contentResolver: ContentResolver): List<Message> {
    val uri = Uri.parse("content://sms/inbox")
    val cursor: Cursor? = contentResolver.query(
        uri,
        arrayOf("DISTINCT address","body", "date"), // DISTINCT
        "address IS NOT NULL) GROUP BY (address", //GROUP BY,
        null,
        "date DESC" // Show newest message at the top
    )
    val messages = mutableListOf<Message>()
    if (cursor != null && cursor.moveToFirst()) {
        do {
            val message = Message(
                cursor.getStringForColumn("address"),
                cursor.getStringForColumn("body"),
                cursor.getStringForColumn("date").toLongOrNull() ?: 0
            )
            //println("SMS from: $message")
            messages.add(message)
        } while (cursor.moveToNext())
        cursor.close()
    }

    return messages
}

/**
 * Returns list of message for passed [sender]
 */
fun getSmsBySender(contentResolver: ContentResolver, sender: String): List<Message> {
    val uri = Uri.parse("content://sms/inbox")
    // TODO: Get SMS send to this sender by our user
    val cursor: Cursor? = contentResolver.query(
        uri,
        arrayOf("address","body", "date"),
        "address='$sender'",
        null,
        null
    )

    val messages = mutableListOf<Message>()
    if (cursor != null && cursor.moveToFirst()) {
        do {
            val message = Message(
                cursor.getStringForColumn("address"),
                cursor.getStringForColumn("body"),
                cursor.getStringForColumn("date").toLongOrNull() ?: 0
            )
            //println("SMS from: $message")
            messages.add(message)
        } while (cursor.moveToNext())
        cursor.close()
    }

    return messages
}