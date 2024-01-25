package com.ndhunju.relay.api

import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.Flow

/**
 * Class with dummy implementation of [ApiInterface]
 */
object ApiInterfaceDummyImpl: ApiInterface {

    override fun createUser(
        name: String?,
        email: String?,
        phone: String?,
        deviceId: String?,
        pushNotificationToken: String?
    ): Flow<Result> {
        return returnFailure()
    }

    override fun updateUser(name: String?, phone: String?): Flow<Result> {
        return returnFailure()
    }

    override fun pairWithParent(childUserId: String, parentEmailAddress: String): Flow<Result> {
        return returnFailure()
    }

    override fun fetchChildUsers(parentUserId: String): Flow<Result> {
        return returnFailure()
    }

    override fun fetchMessagesFromChildUsers(childUserIds: List<String>): Flow<Result> {
        return returnFailure()
    }

    override fun pushMessage(message: Message): Flow<Result> {
        return returnFailure()
    }

    private fun returnFailure(): Flow<Result> {
        return returnFailure()
    }
}