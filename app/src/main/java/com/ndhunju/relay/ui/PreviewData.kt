package com.ndhunju.relay.ui

import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.parent.Child

/** See https://stackoverflow.com/questions/346372/whats-the-difference-between-faking-mocking-and-stubbing
 * Fake might be not a right prefix here. Reconsider it later. May be can use dummyMessages **/
val fakeMessages = listOf(
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
        "ThreadId22",
        "Nabil Bank",
        "This is a sample long messages that should overflow to the next line at the minimum and be start aligned",
        System.currentTimeMillis(),
        "2",
        Result.Success()
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
        "ThreadId23",
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
        "ThreadId32",
        "Bikesh",
        "Hi there!",
        System.currentTimeMillis(),
        "1",
        Result.Success()
    )
)

val mockChildUsers = mutableListOf<Child>().apply {
    add(Child("0", "+14083207200", "key"))
    add(Child("1", "+9779841294000"))
}
