package com.ndhunju.relay.ui

/**
 * List of all possible screens that the app displays
 */
sealed class Screen(open val route: String) {
    data object Welcome: Screen("welcome")
    data object MessageThread: Screen("messageThread")
    data object PairWithParent: Screen("pairWithParent")
    data object Account: Screen("account")

    class MessagesFrom(val threadId: String): Screen(routeWithPlaceHolders) {

        override val route: String
            get() = routeWithPlaceHolders
                .replacePathPlaceholder(threadIdKey, threadId)

        companion object {
            var threadIdKey = "threadIdKey"
            val routeWithPlaceHolders = "messageFrom/"
                .addPathPlaceholder(threadIdKey)
        }

    }

    data object Debug: Screen("debug")
    data object ChildUserList: Screen("childUserList")

    class MessagesFromChild(
        private val userId: String,
        private val phone: String
    ): Screen(routeWithPlaceHolders) {

        override val route: String
            get() = routeWithPlaceHolders
                .replacePathPlaceholder(userIdKey, userId)
                .replaceQueryPlaceholder(phoneKey, phone)

        companion object {
            var userIdKey = "userId"
            var phoneKey = "phone"
            val routeWithPlaceHolders = "messagesFromChild/"
                .addPathPlaceholder(userIdKey)
                .addQueryPlaceholder(phoneKey)
        }
    }

    class MessagesInThreadFromChild(
        private val childUserId: String,
        private val threadId: String,
        private val senderAddress: String
    ) : Screen(routeWithPlaceHolders) {

        override val route: String
            get() = routeWithPlaceHolders
                .replacePathPlaceholder(keyChildUserId, childUserId)
                .replacePathPlaceholder(keyThreadId, threadId)
                .replaceQueryPlaceholder(keySenderAddress, senderAddress)

        companion object {
            var keyChildUserId = "childUserId"
            var keyThreadId = "threadId"
            var keySenderAddress = "senderAddress"
            val routeWithPlaceHolders = "messagesInThreadFromChild/"
                .addPathPlaceholder(keyChildUserId)
                .addPathPlaceholder(keyThreadId)
                .addQueryPlaceholder(keySenderAddress)
        }
    }

}

/**
 * Adds placeholder in the path with correct format.
 * Eg. "baseUrl/".addPathPlaceholder("userId") returns "baseUrl/{userId}"
 */
private fun String.addPathPlaceholder(key: String): String {
    return "$this{$key}/"
}

/**
 * Replaces path placeholder [key] in the path with [value].
 * Eg. "baseUrl/{userId}".replacePathPlaceholder("userId", "1") returns "baseUrl/1"
 */
private fun String.replacePathPlaceholder(key: String, value: String): String {
    return this.replace("{$key}", value)
}

/**
 * Adds query placeholder with correct format
 * Eg. "baseUrl/1?".addQueryPlaceholder("phone") returns "baseUrl/1?phone={phone}"
 */
private fun String.addQueryPlaceholder(key: String): String {
    return "$this$key={$key}&"
}

/**
 * Replaces query placeholder [key] in the query params with [value].
 * Eg. "baseUrl/1?phone={phone}".replaceQueryPlaceholder("phone", "4083207200") returns "baseUrl/1?phone=4083207200"
 */
private fun String.replaceQueryPlaceholder(key: String, value: String): String {
    return this.replace("{$key}", value)
}