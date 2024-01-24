package com.ndhunju.relay.api

import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Class with dummy implementation of [ApiInterface]
 */
class ApiInterfaceDummyImpl: ApiInterface {

    override fun createUser(
        name: String?,
        email: String?,
        phone: String?,
        deviceId: String?,
        pushNotificationToken: String?
    ): StateFlow<Result> {
        return MutableStateFlow(Result.Failure(Throwable("This is a dummy implementation")))
    }

    override fun updateUser(name: String?, phone: String?): StateFlow<Result> {
        return MutableStateFlow(Result.Failure(Throwable("This is a dummy implementation")))
    }

    override fun pairWithParent(childUserId: String, parentEmailAddress: String): Flow<Result> {
        return MutableStateFlow(Result.Failure(Throwable("This is a dummy implementation")))
    }

    override fun pushMessage(message: Message): StateFlow<Result> {
        return MutableStateFlow(Result.Failure(Throwable("This is a dummy implementation")))
    }
}