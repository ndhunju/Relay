package com.ndhunju.relay.service.analyticsprovider

import android.util.Log
import com.ndhunju.relay.BuildConfig

/**
 * Implements [AnalyticsProvider] using [Log].
 * It basically logs all method invocations and parameters to loggger
 */
class LocalAnalyticsProvider: AnalyticsProvider {

    /**
     * Log everything for debug build. Otherwise, anything with [Level.INFO] and above
     */
    override var logLevel = if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO

    override fun logEvent(name: String, message: String?) {
        Log.d(TAG, "logEvent: name:$name; message=$message")
    }

    override fun log(level: Level, tag: String, message: String) {

        if (isLoggable(level).not()) {
            return
        }

        when (level) {
            Level.DEBUG -> Log.d(tag, message)
            Level.INFO -> Log.i(tag, message)
            Level.ERROR -> Log.e(tag, message)
            Level.NONE -> {}
        }
    }

    override fun setUserId(userId: String?) {
        // Can be ignored
    }

    companion object {
        val TAG: String = LocalAnalyticsProvider::class.java.simpleName
    }

}