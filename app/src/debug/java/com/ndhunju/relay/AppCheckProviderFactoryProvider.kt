package com.ndhunju.relay

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class AppCheckProviderFactoryProvider {
    fun provide(): AppCheckProviderFactory {
        // See https://firebase.google.com/docs/app-check/android/debug-provider
        // to know how to make App Check work on emulator
        return DebugAppCheckProviderFactory.getInstance()
    }
}