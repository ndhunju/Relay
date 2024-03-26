package com.ndhunju.relay.service

import androidx.annotation.OpenForTesting
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.service.analyticsprovider.CrashlyticsAnalyticsProvider
import com.ndhunju.relay.service.analyticsprovider.FirebaseAnalyticsProvider
import com.ndhunju.relay.service.analyticsprovider.Level
import com.ndhunju.relay.service.analyticsprovider.LocalAnalyticsProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all [AnalyticsProvider]
 */
@Singleton
@OpenForTesting // To be able to mock it, needs to be open
open class AnalyticsManager @Inject constructor(): AnalyticsProvider {

    override var logLevel = Level.NONE

    /**
     * List of all [AnalyticsProvider]
     */
    private val analyticsProviders by lazy {
        arrayOf(
            LocalAnalyticsProvider(),
            FirebaseAnalyticsProvider(),
            CrashlyticsAnalyticsProvider()
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

    override fun setLevel(level: Level) {
        analyticsProviders.forEach { analyticsProvider ->
            analyticsProvider.setLevel(level)
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

    override fun log(level: Level, tag: String, message: String) {
        analyticsProviders.forEach { analyticsProvider ->
            analyticsProvider.log(level, tag, message)
        }
    }

}