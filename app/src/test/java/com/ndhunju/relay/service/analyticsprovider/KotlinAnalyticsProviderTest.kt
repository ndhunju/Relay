package com.ndhunju.relay.service.analyticsprovider

/**
 * Implements [AnalyticsProvider] using [println].
 * It basically prints all method invocations and parameters to the logger.
 * Can pass this for unit testing purposes
 */
class KotlinAnalyticsProvider: AnalyticsProvider {

    override var logLevel: Level = Level.DEBUG

    override fun setUserId(userId: String?) {}

    override fun logEvent(name: String, message: String?) {
        println("Event: $name; message: $message")
    }

    override fun log(level: Level, tag: String, message: String) {
        println("$level: $tag, message=$message")
    }

}