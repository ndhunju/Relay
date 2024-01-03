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
fun readSms(contentResolver: ContentResolver): List<Message> {
    val uri = Uri.parse("content://sms/inbox")
    val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
    val messages = mutableListOf<Message>()
    if (cursor != null && cursor.moveToFirst()) {
        do {
            val message = Message(
                cursor.getStringForColumn("address"),
                cursor.getStringForColumn("body"),
                cursor.getStringForColumn("date").toLong()
            )
            //println("SMS from: $message")
            messages.add(message)
        } while (cursor.moveToNext())
        cursor.close()
    }

    return messages
}