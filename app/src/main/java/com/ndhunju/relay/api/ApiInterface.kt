package com.ndhunju.relay.api

import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.util.User

/**
 * Abstracts the API that allows interaction with some kind of BE server
 */
interface ApiInterface {

    /**
     * Makes API request to create user.
     */
    suspend fun postUser(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        deviceId: String? = null,
        pushNotificationToken: String? = null,
    ): Result<String>

    /**
     * Makes API request to update the user. If null is passed, don't update that value.
     * Only the non-null value must be updated in the server
     */
    suspend fun putUser(name: String? = null, phone: String? = null, ): Result<Void>


    /**
     * Makes API request to pair [childUserId] with a
     * parent user whose email is [parentEmailAddress].
     * Returns parent user's id in [Result.Success.data]
     */
    suspend fun postPairWithParent(childUserId: String, parentEmailAddress: String): Result<String>

    /**
     * Makes API request to unpair [childUserId] with a
     * parent with id [parentUserId].
     */
    suspend fun postUnPairWithParent(childUserId: String, parentUserId: String): Result<Boolean>

    /**
     * Makes API request to pair with a Child with email [childEmailAddress]
     * and pairing code [pairingCode]
     * Returns child user's id in [Result.Success.data]
     */
    @Deprecated("Pairing with child from parent client involves too many edge cases")
    suspend fun postPairWithChild(childEmailAddress: String, pairingCode: String): Result<String>

    /**
     * Makes API request to get parent users for passed [childUserId]
     */
    suspend fun getParentUsers(childUserId: String): Result<List<User>>

    /**
     * Makes API request to fetch all the paired child users.
     * @param parentUserId : Id of the parent user for which child users needed to be fetched
     */
    suspend fun getChildUsers(parentUserId: String): Result<List<Child>>

    /**
     * Makes API request to fetch all [Message]s sent by [childUserId]
     */
    suspend fun getMessagesFromChildUser(childUserId: String): Result<List<MessageEntry>>

    /**
     * Makes API request to notify the back end that this client did save the messages
     * with id in [childSmsInfoList]
     */
    suspend fun postDidSaveFetchedMessages(childSmsInfoList: List<ChildSmsInfo>): Result<Void>

    /**
     * Pushes [message] to the server.
     */
    suspend fun postMessage(message: MessageEntry): Result<Void>

    /**
     * Posts [token] to the server
     */
    suspend fun postUserPushNotificationToken(token: String): Result<Void>

}