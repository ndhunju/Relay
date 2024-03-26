package com.ndhunju.relay.service.analyticsprovider

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.ndhunju.relay.BuildConfig

class CrashlyticsAnalyticsProvider: AnalyticsProvider {

    override var logLevel: Level = if (BuildConfig.DEBUG) {
        Level.NONE // Don't send any crash logs for debug builds
    } else {
        Level.DEBUG // Send all logs for release builds
    }

    override fun setUserId(userId: String?) {
        if (userId != null) {
            Firebase.crashlytics.setUserId(userId)
        }
    }

    override fun logEvent(name: String, message: String?) {
        // Record exceptions that happens in the app
        if (name.startsWith("didCatch") || name.startsWith("didFail")) {
            Firebase.crashlytics.recordException(Throwable(name))
        }
        Firebase.crashlytics.log("Event=$name;M=$message")
    }

    override fun log(level: Level, tag: String, message: String) {
        if (isLoggable(level).not()) {
            return
        }

        Firebase.crashlytics.log("L=$level,T=$tag,M= $message")
    }
}