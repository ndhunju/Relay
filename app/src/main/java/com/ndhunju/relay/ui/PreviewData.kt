package com.ndhunju.relay.ui

import com.ndhunju.relay.service.Result
import com.ndhunju.relay.ui.messages.Message

val mockMessages = listOf(
    Message(
        "0",
        "ThreadId1",
        "Bikesh",
        "See you soon!",
        System.currentTimeMillis().toString(),
        "1",
        Result.Success()
    ),
    Message(
        "1",
        "ThreadId2",
        "Nabil Bank",
        "2,300.00 added to your account. Your new balance is 50,000",
        System.currentTimeMillis().toString(),
        "2",
        Result.Success()
    ),
    Message(
        "1",
        "ThreadId2",
        "Bikesh",
        "This is a sample long messages that should overflow to the next line at the minimum",
        System.currentTimeMillis().toString(),
        "1",
        Result.Success()
    )
)
