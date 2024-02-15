package com.ndhunju.relay

import android.app.Application
import com.ndhunju.relay.di.AndroidAppModule
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.di.AppModule
import com.ndhunju.relay.di.DaggerAppComponent
import com.ndhunju.relay.util.worker.SecondaryAppStartTasksWorker
import com.ndhunju.relay.util.worker.SecondaryAppStartTasksWorker.Companion.DELAY_IN_MILLIS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class RelayApplication: Application() {

    // appComponent lives in the Application class to share its lifecycle
    // Reference to the application graph that is used across the whole app
    lateinit var appComponent: AppComponent
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        setUpDaggerAppComponent()
        setUpAnalyticsManager()
        doEnqueueSecondaryAppStartTasksWorker()

        // TODO: Nikesh - Based on user's id, always fetch latest user info from server
    }

    private fun setUpDaggerAppComponent() {
        this.appComponent = DaggerAppComponent.builder()
            .androidAppModule(AndroidAppModule(this))
            .appModule(AppModule(this))
            .build()
    }

    /**
     * Enqueues tasks that need to be run at app start up but could be delayed for
     * [DELAY_IN_MILLIS]. This way, the app isn't doing too many work at start up
     * which would increase app start up time.
     */
    private fun doEnqueueSecondaryAppStartTasksWorker() {
        // Useful when debugging to make sure only up-to-date workers exists in WorkerManager's database
        //appComponent.workManager().cancelAllWork()
        SecondaryAppStartTasksWorker.enqueue(appComponent.workManager(), applicationScope)
    }

    private fun setUpAnalyticsManager() {
        appComponent.analyticsManager().setUserId(appComponent.currentUser().user.id)
    }
}