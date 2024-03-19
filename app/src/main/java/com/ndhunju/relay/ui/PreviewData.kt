package com.ndhunju.relay.ui

import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.parent.Child

val mockMessages = listOf(
    Message(
        "0",
        "ThreadId1",
        "Bikesh",
        "See you soon!",
        System.currentTimeMillis(),
        "1",
        Result.Failure()
    ),
    Message(
        "1",
        "ThreadId2",
        "Nabil Bank",
        "Your new balance is 50,000",
        System.currentTimeMillis(),
        "2",
        Result.Success()
    ),
    Message(
        "1",
        "ThreadId2",
        "Nabil Bank",
        "2,300.00 added to your account.",
        System.currentTimeMillis(),
        "2",
        Result.Success()
    ),
    Message(
        "2",
        "ThreadId3",
        "Bikesh",
        "This is a sample long messages that should overflow to the next line at the minimum",
        System.currentTimeMillis(),
        "1",
        Result.Success()
    ),
    Message(
        "2",
        "ThreadId3",
        "Bikesh",
        "Hi there!",
        System.currentTimeMillis(),
        "1",
        Result.Success()
    )
)

val mockChildUsers = mutableListOf<Child>().apply {
    add(Child("0", "emailid1@gmail.com", "key"))
    add(Child("1", "emailid2@gmail.com"))
}
