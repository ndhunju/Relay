package com.ndhunju.relay.ui

/**
 * List of all possible screens that the app displays
 */
sealed class Screen(open val route: String) {
    data object MessageThread: Screen("messageThread")
    data object PairWithParent: Screen("pairWithParent")
    data object Account: Screen("account")

    class MessagesFrom(val threadId: String): Screen(pattern) {

        override val route: String
            get() = pattern.replace("{$id}", threadId)

        companion object {
            var id = "id"
            val pattern = "messageFrom/{$id}"
        }
    }

    data object Debug: Screen("debug")
}