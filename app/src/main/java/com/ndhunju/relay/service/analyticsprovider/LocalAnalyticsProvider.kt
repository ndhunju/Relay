package com.ndhunju.relay.service

import android.util.Log
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider

/**
 * Implements [AnalyticsProvider] using [Log].
 * It basically logs all method invocations and parameters to loggger
 */
class LocalAnalyticsProvider: AnalyticsProvider {

    override fun logEvent(name: String, message: String?) {
        Log.d(TAG, "logEvent: name:$name; message=$message")
    }

    override fun setUserId(userId: String?) {
        // Can be ignored
    }

    companion object {
        val TAG: String = LocalAnalyticsProvider::class.java.simpleName
    }

}