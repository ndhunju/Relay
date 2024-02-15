package com.ndhunju.relay.api

import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.ui.messages.Message

/**
 * Abstracts the API that allows interaction with some kind of BE server
 */
interface ApiInterface {

    /**
     * Makes API request to create user.
     */
    suspend fun createUser(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        deviceId: String? = null,
        pushNotificationToken: String? = null,
    ): Result

    /**
     * Makes API request to update the user. If null is passed, don't update that value.
     * Only the non-null value must be updated in the server
     */
    suspend fun updateUser(name: String? = null, phone: String? = null, ): Result


    /**
     * Makes API request to pair [childUserId] with a
     * parent user whose email is [parentEmailAddress]
     */
    suspend fun pairWithParent(childUserId: String, parentEmailAddress: String): Result

    /**
     * Makes API request to fetch all the paired child users.
     * @param parentUserId : Id of the parent user for which child users needed to be fetched
     */
    suspend fun fetchChildUsers(parentUserId: String): Result

    /**
     * Makes API request to fetch all [Message]s sent by [childUserIds]
     */
    suspend fun fetchMessagesFromChildUsers(childUserIds: List<String>): Result

    /**
     * Makes API request to notify the back end that this client did save the messages
     * with id in [childSmsInfoList]
     */
    suspend fun notifyDidSaveFetchedMessages(childSmsInfoList: List<ChildSmsInfo>): Result

    /**
     * Pushes [message] to the server.
     */
    suspend fun pushMessage(message: Message): Result

    /**
     * Posts [token] to the server
     */
    suspend fun postUserPushNotificationToken(token: String): Result

}