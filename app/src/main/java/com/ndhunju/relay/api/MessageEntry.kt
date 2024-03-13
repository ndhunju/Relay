package com.ndhunju.relay.api

import com.google.gson.annotations.SerializedName

/**
 * Represent fields of Message collection
 */
class MessageEntry(
    var idInServer: String = "",
    @SerializedName(value = SenderUserId)
    val senderUserId: String,
    @SerializedName(value = SentDate)
    val sentDate: String,
    @SerializedName(value = PayLoad)
    val payLoad: String
) {
    companion object {
        const val SenderUserId = "SenderUserId"
        const val SentDate = "SentDate"
        const val PayLoad = "PayLoad"
    }
}