package com.ndhunju.relay.util.worker

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.service.analyticsprovider.d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

/**
 * Performs tasks that needs to be done at app start up but could be delayed for [DELAY_IN_MILLIS].
 */
class SecondaryAppStartTasksWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val appComponent: AppComponent by lazy {
        (applicationContext as RelayApplication).appComponent
    }

    private val analyticsProvider: AnalyticsProvider by lazy {
        appComponent.analyticsProvider()
    }

    private val simpleKeyValuePersistService: SimpleKeyValuePersistService by lazy {
        appComponent.simpleKeyValuePersistService()
    }

    override suspend fun doWork(): Result {
        analyticsProvider.d(TAG, "doWork() start")
        saveAppInstallTime()
        // UriTriggerWorker.enqueue(this)
        GetLatestSettingsWorker.doEnqueueWorker(appComponent.workManager())
        NewMessageNotifierWorker.doEnqueueWorker(appComponent.workManager())
        UploadNewMessagesWorker.doEnqueueWorkerToUploadNewMessages(appComponent.workManager())
        analyticsProvider.d(TAG, "doWork() finish")
        return Result.success()
    }

    /**
     * Save the time when the app was first installed
     */
    private suspend fun saveAppInstallTime() {
        analyticsProvider.d(TAG, "saveAppInstallTime()")
        simpleKeyValuePersistService.saveFirstTime(
            KEY_APP_INSTALL_TIME,
            System.currentTimeMillis().toString()
        )
    }

    companion object {
        const val DELAY_IN_MILLIS = 3000L // 3 seconds
        val TAG: String = SecondaryAppStartTasksWorker::class.java.simpleName

        fun enqueue(workManager: WorkManager, scope: CoroutineScope) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                workManager.enqueueUniqueWork(
                    TAG,
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<SecondaryAppStartTasksWorker>()
                        .setInitialDelay(Duration.ofMillis(DELAY_IN_MILLIS))
                        .addTag(TAG)
                        .build()
                )
            } else {
                scope.launch(Dispatchers.IO) {
                    delay(DELAY_IN_MILLIS)
                    workManager.enqueueUniqueWork(
                        TAG,
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequestBuilder<SecondaryAppStartTasksWorker>()
                            .addTag(TAG)
                            .build()
                    )
                }
            }

        }
    }
}

/**
 * Key used for storing app install time in [SimpleKeyValuePersistService]
 */
const val KEY_APP_INSTALL_TIME = "appInstallTime"