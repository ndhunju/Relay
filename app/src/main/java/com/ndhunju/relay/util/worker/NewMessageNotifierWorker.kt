package com.ndhunju.relay.util.worker

import android.content.Context
import android.provider.Telephony
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.service.AppStateBroadcastService
import java.util.concurrent.TimeUnit

/**
 * Notifies observer of [AppStateBroadcastService.newMessagesReceivedTime] that new SMS are received
 */
class NewMessageNotifierWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val appComponent: AppComponent by lazy {
        (applicationContext as RelayApplication).appComponent
    }

    private val appStateBroadcastService: AppStateBroadcastService by lazy {
        appComponent.appStateBroadcastService()
    }

    override suspend fun doWork(): Result {
        val approxTimeOfNewMessage = System.currentTimeMillis() - MAX_DELAY_MILLIS
        // Notify the observers
        appStateBroadcastService.updateNewMessagesReceivedTime(approxTimeOfNewMessage)
        // Enqueue again to process new changes in Sms.Sms.CONTENT_URI since last SMS DB read
        doEnqueueWorker(appComponent.workManager())
        return Result.success()
    }

    companion object {

        private val TAG: String = NewMessageNotifierWorker::class.java.simpleName
        private const val MAX_DELAY_MILLIS = 300L

        private val constraints: Constraints by lazy {
            Constraints.Builder()
                .addContentUriTrigger(Telephony.Sms.CONTENT_URI, true)
                .setTriggerContentMaxDelay(MAX_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .setTriggerContentUpdateDelay(MAX_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .build()
        }

        fun doEnqueueWorker(workManager: WorkManager) {
            workManager.enqueueUniqueWork(
                TAG,
                // NOTE: APPEND isn't triggering the worker at all
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<NewMessageNotifierWorker>()
                    .addTag(TAG)
                    .setConstraints(constraints)
                    .build()
            )
        }

    }
}