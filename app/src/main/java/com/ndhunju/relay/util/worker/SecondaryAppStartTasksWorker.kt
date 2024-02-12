package com.ndhunju.relay.util.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.di.AppComponent

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

    override suspend fun doWork(): Result {
        saveAppInstallTime()
        UploadNewMessagesWorker.doEnqueueWorkerToUploadNewMessages(appComponent)
        return Result.success()
    }

    /**
     * Save the time when the app was first installed
     */
    private suspend fun saveAppInstallTime() {
        appComponent.simpleKeyValuePersistService().saveFirstTime(
            "appInstallTime",
            System.currentTimeMillis().toString()
        )
    }

    companion object {
        const val DELAY_IN_MILLIS = 3000L // 3 seconds
    }
}