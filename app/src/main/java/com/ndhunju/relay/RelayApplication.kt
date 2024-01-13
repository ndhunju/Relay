package com.ndhunju.relay

import android.app.Application
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User

class RelayApplication: Application() {

    // appComponent lives in the Application class to share its lifecycle
    // Reference to the application graph that is used across the whole app
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        this.appComponent = DaggerAppComponent.builder()
            .androidAppModule(AndroidAppModule(this))
            .appModule(AppModule(this))
            .build()

        // Instantiate user in CurrentUser as app is dependent on it to function
        CurrentUser.user = appComponent.userSettingsPersistService().retrieve() ?: User()
    }
}