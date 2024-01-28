package com.ndhunju.relay.api

import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Class with dummy implementation of [ApiInterface]
 */
object ApiInterfaceDummyImpl: ApiInterface {

    override suspend fun createUser(
        name: String?,
        email: String?,
        phone: String?,
        deviceId: String?,
        pushNotificationToken: String?
    ): Result {
        return returnFailure()
    }

    override suspend fun updateUser(name: String?, phone: String?): Result {
        return returnFailure()
    }

    override suspend fun pairWithParent(childUserId: String, parentEmailAddress: String): Result {
        return returnFailure()
    }

    override suspend fun fetchChildUsers(parentUserId: String): Result {
        return returnFailure()
    }

    override suspend fun fetchMessagesFromChildUsers(childUserIds: List<String>): Result {
        return returnFailure()
    }

    override suspend fun notifyDidSaveFetchedMessages(messageIds: List<String>): Result {
        return returnFailure()
    }

    override suspend fun pushMessage(message: Message): Result {
        return returnFailure()
    }

    private fun returnFailure(): Result {
        return Result.Failure(Throwable("This is a dummy implementation"))
    }
}