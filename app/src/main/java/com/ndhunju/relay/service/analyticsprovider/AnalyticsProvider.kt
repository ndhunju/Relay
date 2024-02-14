package com.ndhunju.relay.service.analyticsprovider

/**
 * Lists API that any [AnalyticsProvider] should implement
 */
interface AnalyticsProvider {

    var logLevel: Level

    fun setUserId(userId: String?)

    fun setLevel(level: Level) {
        logLevel = level
    }

    /**
     * Client invokes this to log an event with [name] and [message]
     */
    fun logEvent(name: String, message: String? = null)

    /**
     * Client invokes this to log
     */
    fun log(level: Level, tag: String, message: String)

}

/**
 * Levels of log supported
 */
enum class Level {
    DEBUG,
    INFO,
    ERROR,

    /**
     * Use this to log nothing
     */
    NONE
}

//region Convenient Extension Functions

fun AnalyticsProvider.isLoggable(level: Level): Boolean {
    return level >= logLevel
}

fun AnalyticsProvider.d(tag: String, message: String) {
    log(Level.DEBUG, tag, message)
}

fun AnalyticsProvider.i(tag: String, message: String) {
    log(Level.INFO, tag, message)
}

fun AnalyticsProvider.e(tag: String, message: String) {
    log(Level.ERROR, tag, message)
}

//endregion