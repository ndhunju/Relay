package com.ndhunju.relay.api

import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.util.User

/**
 * Class with dummy implementation of [ApiInterface]
 */
object ApiInterfaceDummyImpl : ApiInterface {

    override suspend fun postUser(
        name: String?,
        phone: String?
    ): Result<String> {
        return returnFailure()
    }

    override suspend fun putUser(name: String?): Result<Void> {
        return returnFailure()
    }

    override suspend fun postPairWithParent(
        childUserId: String,
        parentEmailAddress: String
    ): Result<String> {
        return returnFailure()
    }

    override suspend fun postUnPairWithParent(
        childUserId: String,
        parentUserId: String
    ): Result<Boolean> {
        return returnFailure()
    }

    @Deprecated("See parent method")
    override suspend fun postPairWithChild(
        childPhoneNumber: String,
        pairingCode: String
    ): Result<String> {
        return returnFailure()
    }

    override suspend fun getParentUsers(childUserId: String): Result<List<User>> {
        return returnFailure()
    }

    override suspend fun getChildUsers(parentUserId: String): Result<List<Child>> {
        return returnFailure()
    }

    override suspend fun getMessagesFromChildUser(childUserId: String): Result<List<MessageEntry>> {
        return returnFailure()
    }

    override suspend fun postDidSaveFetchedMessages(childSmsInfoList: List<ChildSmsInfo>): Result<Void> {
        return returnFailure()
    }

    override suspend fun postMessage(message: MessageEntry): Result<Void> {
        return returnFailure()
    }

    override suspend fun postUserPushNotificationToken(token: String): Result<Void> {
        return returnFailure()
    }

    private fun <T> returnFailure(): Result<T> {
        return Result.Failure(Throwable("This is a dummy implementation"))
    }
}