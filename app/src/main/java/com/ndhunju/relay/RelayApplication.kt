package com.ndhunju.relay

import android.app.Application
import com.ndhunju.relay.di.AndroidAppModule
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.di.AppModule
import com.ndhunju.relay.di.DaggerAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RelayApplication: Application() {

    // appComponent lives in the Application class to share its lifecycle
    // Reference to the application graph that is used across the whole app
    lateinit var appComponent: AppComponent
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        setUpDaggerAppComponent()
        saveAppInstallTime()

        // TODO: Nikesh - Based on user's id, always fetch latest user info from server
    }

    private fun setUpDaggerAppComponent() {
        this.appComponent = DaggerAppComponent.builder()
            .androidAppModule(AndroidAppModule(this))
            .appModule(AppModule(this))
            .build()
    }

    /**
     * Save the time when the app was first installed
     */
    private fun saveAppInstallTime() {
        applicationScope.launch(Dispatchers.IO) {
            appComponent.simpleKeyValuePersistService().saveFirstTime(
                "appInstallTime",
                System.currentTimeMillis().toString()
            )
        }
    }
}