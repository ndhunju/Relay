package com.ndhunju.relay

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class AppCheckProviderFactoryProvider {
    fun provide(): AppCheckProviderFactory {
        return PlayIntegrityAppCheckProviderFactory.getInstance()
    }
}