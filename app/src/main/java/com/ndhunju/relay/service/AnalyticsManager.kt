package com.ndhunju.relay.service

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all [AnalyticsProvider]
 */
@Singleton
class AnalyticsManager @Inject constructor(): AnalyticsProvider {

    /**
     * List of all [AnalyticsProvider]
     */
    private val analyticsProviders by lazy {
        arrayOf(
            LocalAnalyticsProvider(),
            FirebaseAnalyticsProvider()
        )
    }

    /**
     * Sets user id to all [AnalyticsProvider].
     * This could be helpful when debugging issue of a specific user
     */
    override fun setUserId(userId: String?) {
        analyticsProviders.forEach { analyticsProvider ->
            analyticsProvider.setUserId(userId)
        }
    }

    /**
     * Invokes [AnalyticsProvider.logEvent] of all the added [AnalyticsProvider]
     */
    override fun logEvent(name: String, message: String?) {
        analyticsProviders.forEach { analyticsProvider ->
            analyticsProvider.logEvent(name, message)
        }
    }

}