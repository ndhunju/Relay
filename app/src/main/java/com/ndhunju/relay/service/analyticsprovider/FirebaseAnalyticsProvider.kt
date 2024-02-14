package com.ndhunju.relay.service.analyticsprovider

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

/**
 * Implements [AnalyticsProvider] using [Firebase]
 */
class FirebaseAnalyticsProvider: AnalyticsProvider {

    /**
     * Log only anything of [Level.INFO] and above in Firebase
     */
    override var logLevel = Level.INFO

    override fun setUserId(userId: String?) {
        Firebase.analytics.setUserId(userId)
    }

    override fun logEvent(name: String, message: String?) {
        val bundle = Bundle()
        bundle.putString("message", message)
        Firebase.analytics.logEvent(name, bundle)
    }

    override fun log(level: Level, tag: String, message: String) {

        if (isLoggable(level).not()) {
            return
        }

        val bundle = Bundle()
        bundle.putString("tag", tag)
        bundle.putString("message", message)
        Firebase.analytics.logEvent("Log", bundle)

    }
}