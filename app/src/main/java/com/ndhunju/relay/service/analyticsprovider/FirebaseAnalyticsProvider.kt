package com.ndhunju.relay.service.analyticsprovider

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.app
import com.ndhunju.relay.R

/**
 * Implements [AnalyticsProvider] using [Firebase]
 */
class FirebaseAnalyticsProvider: AnalyticsProvider {

    private val appName by lazy {
        Firebase.app.applicationContext.getString(R.string.app_name).lowercase()
    }

    /**
     * Prefix with app name so that it is easier to filter in Analytics Dashboard
     */
    private val eventLog by lazy {
        "${appName}_log"
    }

    private val messageParam by lazy {
        "${appName}_param_message"
    }

    private val tagParam by lazy {
        "${appName}_param_tag"
    }


    /**
     * Log only anything of [Level.INFO] and above in Firebase
     */
    override var logLevel = Level.INFO

    override fun setUserId(userId: String?) {
        Firebase.analytics.setUserId(userId)
    }

    override fun logEvent(name: String, message: String?) {
        val bundle = Bundle()
        // Firebase Analytics might crop the message if it is too long
        // See Crashlytics for more details logs
        bundle.putString(messageParam, message)
        Firebase.analytics.logEvent(name, bundle)
    }

    override fun log(level: Level, tag: String, message: String) {

        if (isLoggable(level).not()) {
            return
        }

        val bundle = Bundle()
        bundle.putString(tagParam, tag)
        bundle.putString(messageParam, message)
        // Firebase Analytics might crop the message if it is too long
        // See Crashlytics for more details logs
        Firebase.analytics.logEvent(eventLog, bundle)

    }
}