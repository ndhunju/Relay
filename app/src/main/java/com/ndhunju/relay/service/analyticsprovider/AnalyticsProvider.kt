package com.ndhunju.relay.service.analyticsprovider

/**
 * Lists API that any [AnalyticsProvider] should implement
 */
interface AnalyticsProvider {

    /**
     * Client invokes this to log an event with [name] and [message]
     */
    fun logEvent(name: String, message: String? = null)

    fun setUserId(userId: String?)
}