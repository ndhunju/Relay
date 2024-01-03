package com.ndhunju.relay.ui

import com.ndhunju.relay.ui.messages.Message

val mockMessages = listOf(
    Message(
        "Sanima Bank",
        "2,300.00 withdrawn from your account. Your new balance is 46,000",
        System.currentTimeMillis().toString()
    ),
    Message(
        "Nabil Bank",
        "2,300.00 added to your account. Your new balance is 50,000",
        System.currentTimeMillis().toString()
    )
)
