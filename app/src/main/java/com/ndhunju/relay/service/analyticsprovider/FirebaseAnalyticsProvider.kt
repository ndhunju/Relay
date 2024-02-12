package com.ndhunju.relay.service

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider

/**
 * Implements [AnalyticsProvider] using [Firebase]
 */
class FirebaseAnalyticsProvider: AnalyticsProvider {

    override fun logEvent(name: String, message: String?) {
        val bundle = Bundle()
        bundle.putString("message", message)
        Firebase.analytics.logEvent(name, bundle)
    }

    override fun setUserId(userId: String?) {
        Firebase.analytics.setUserId(userId)
    }
}