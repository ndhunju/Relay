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
    val uri = Uri.parse("content://sms")
    val cursor: Cursor? = contentResolver.query(
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

/**
 * Returns list of message for passed [sender]
 */
fun getSmsByThreadId(contentResolver: ContentResolver, sender: String): List<Message> {
    val uri = Uri.parse("content://sms")
    val cursor: Cursor? = contentResolver.query(
        uri,
        smsColumns,
        "thread_id='$sender'",
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

val smsColumns = arrayOf("thread_id", "status", "type", "subject", "person", "reply_path_present",
    "address","body", "date")

fun fromCursor(cursor: Cursor): Message {
    return Message(
        cursor.getStringForColumn("thread_id"),
        cursor.getStringForColumn("address"),
        cursor.getStringForColumn("body"),
        cursor.getStringForColumn("date"),
        cursor.getStringForColumn("type"),
        cursor.getStringForColumn("type")
    )
}