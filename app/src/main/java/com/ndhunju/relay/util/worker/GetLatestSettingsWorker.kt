package com.ndhunju.relay.util.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result.Success
import com.ndhunju.relay.api.response.Settings
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.util.CurrentSettings

/**
 * Gets latest [Settings] and stores it in [CurrentSettings]
 */
class GetLatestSettingsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val appComponent: AppComponent by lazy {
        (applicationContext as RelayApplication).appComponent
    }

    private val apiInterFace: ApiInterface by lazy {
        appComponent.apiInterface()
    }

    private val currentSettings: CurrentSettings by lazy {
        appComponent.currentSettings()
    }

    override suspend fun doWork(): Result {
        when (val result = apiInterFace.getSettings()) {
            is Success -> {
                val settings = result.data ?: return Result.failure()
                currentSettings.updateSettings(settings)
                return Result.success()
            }
            else -> {
                return Result.failure()
            }
        }
    }

    companion object {

        val TAG: String = GetLatestSettingsWorker::class.java.simpleName

        fun doEnqueueWorker(workManager: WorkManager) {
            workManager.enqueueUniqueWork(
                TAG,
                // NOTE: APPEND isn't triggering the worker at all
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<GetLatestSettingsWorker>()
                    .addTag(TAG)
                    .build()
            )
        }
    }
}