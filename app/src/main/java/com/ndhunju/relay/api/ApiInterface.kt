package com.ndhunju.relay.api

import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts the API that allows interaction with some kind of BE server
 */
interface ApiInterface {

    /**
     * Makes API request to create user.
     */
    fun createUser(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        deviceId: String? = null,
        pushNotificationToken: String? = null,
    ): StateFlow<Result>

    /**
     * Makes API requests to update the user. If null is passed, don't update that value.
     * Only the non-null value must be updated in the server
     */
    fun updateUser(name: String? = null, phone: String? = null, ): StateFlow<Result>


    /**
     * Makes API request to pair [childUserId] with a
     * parent user whose email is [parentEmailAddress]
     */
    fun pairWithParent(childUserId: String, parentEmailAddress: String): Flow<Result>

    /**
     * Pushes [message] to the server.
     */
    fun pushMessage(message: Message): StateFlow<Result>

}