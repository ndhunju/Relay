package com.ndhunju.relay.service

import android.app.Application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.messages.Message
import java.io.IOException
import java.util.Date

class DeviceSmsReaderServiceMock(
    private val gson: Gson,
    private val application: Application
) : DeviceSmsReaderService {

    val messages: List<Message> by lazy {
        gson.fromJson(
            readFileFromResources(R.raw.messages),
            TypeToken.getParameterized(List::class.java, Message::class.java).type
        )
    }

    override fun getLastMessageForEachThread(): List<Message> {
        val id = mutableSetOf<String>()
        // Filter out messages with duplicate threadId
        return messages.mapNotNull {
            if (id.contains(it.threadId).not()) {
                id.add(it.threadId)
                return@mapNotNull it
            }

            return@mapNotNull null
        }
    }

    override fun getSmsByThreadId(threadId: String): List<Message> {
        return messages.filter { it.threadId == threadId }
    }

    override fun getMessageByAddressAndBody(address: String, body: String): Message {
        return messages.first { it.from == address && it.body == body }
    }

    override fun getMessagesByAddress(address: String): List<Message> {
        return messages.filter { it.from == address }
    }

    override fun getMessagesSince(time: Long): List<Message> {
        val since = Date(time)
        return messages.filter { Date(it.date).after(since) }
    }

    @Throws(IOException::class)
    fun readFileFromResources(id: Int): String {
        return application.resources.openRawResource(id).bufferedReader().use { it.readText() }
    }

}