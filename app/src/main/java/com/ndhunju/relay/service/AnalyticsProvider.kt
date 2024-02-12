package com.ndhunju.relay.service

import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

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