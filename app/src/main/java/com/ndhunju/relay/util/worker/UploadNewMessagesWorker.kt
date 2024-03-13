package com.ndhunju.relay.util.worker

import android.content.Context
import android.provider.Telephony.Sms
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.MessageEntry
import com.ndhunju.relay.api.Result.Success
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.service.AnalyticsManager
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.service.EncryptionService
import com.ndhunju.relay.service.NotificationManager
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import com.ndhunju.relay.service.analyticsprovider.Level
import com.ndhunju.relay.service.analyticsprovider.d
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.toSmsInfo
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.checkIfPermissionGranted
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import kotlin.String
import kotlin.getValue
import kotlin.lazy
import com.ndhunju.relay.api.Result as RelayResult

private const val KEY_LAST_UPLOAD_TIME = "lastUploadTime"

/**
 * Uploads new [Message]s, that is, previously not uploaded messages, to the server.
 * Once the task is finished it notifies [AppStateBroadcastService]
 */
class UploadNewMessagesWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val appComponent: AppComponent by lazy {
        (applicationContext as RelayApplication).appComponent
    }

    private val deviceSmsReaderService: DeviceSmsReaderService by lazy {
        appComponent.deviceSmsReaderService()
    }

    private val smsInfoRepository: SmsInfoRepository by lazy {
        appComponent.smsInfoRepository()
    }

    private val apiInterface: ApiInterface by lazy {
        appComponent.apiInterface()
    }

    private val keyValuePersistService: SimpleKeyValuePersistService by lazy {
        appComponent.simpleKeyValuePersistService()
    }

    private val appStateBroadcastService: AppStateBroadcastService by lazy {
        appComponent.appStateBroadcastService()
    }

    private val notificationManager: NotificationManager by lazy {
        appComponent.notificationManager()
    }

    private val currentUser: CurrentUser by lazy {
        appComponent.currentUser()
    }

    private val analyticsManager: AnalyticsManager by lazy {
        appComponent.analyticsManager()
    }

    private val encryptionService: EncryptionService by lazy {
        appComponent.encryptionService()
    }

    private val gson: Gson by lazy {
        appComponent.gson()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NotificationManager.ID_UPLOAD_NEW_MESSAGES,
            notificationManager.getNotificationForUploadingNewMessages()
        )
    }

    override suspend fun doWork(): Result {
        analyticsManager.d(TAG, "doWork() start")
        if (checkIfPermissionGranted(applicationContext).not()) {
            // No work we can do for now
            return Result.success()
        }

        if (currentUser.user.getParentUsers().isEmpty()) {
            // No parents to forward the messages to
            analyticsManager.d(TAG, "Skipping since no parents found")
            return Result.success()
        }

        var result: RelayResult<Void> = Success()
        val uploadStartTime = System.currentTimeMillis()
        // Retrieve previously saved uploadStartTime
        val lastUploadStartTime = keyValuePersistService.retrieve(
            KEY_LAST_UPLOAD_TIME
        ).firstOrNull()?.toLong() ?: uploadStartTime

        val processedMessages = mutableListOf<Message>()

        // Process new messages that arrived after the last upload time.
        // Ex. If last upload happened yesterday at 8 am, process messages received after that
        deviceSmsReaderService.getMessagesSince(lastUploadStartTime).forEach { messageFromAndroidDb ->
            processedMessages.add(messageFromAndroidDb)
            result += processMessage(messageFromAndroidDb)
        }

        // Enqueue again to process new changes in Sms.Sms.CONTENT_URI since last SMS DB read
        doEnqueueWorkerToUploadNewMessages(appComponent.workManager())

        return if (result is Success) {
            // Save last upload start time
            keyValuePersistService.save(KEY_LAST_UPLOAD_TIME, uploadStartTime.toString())
            // Notify that new messages has been processed
            appStateBroadcastService.updateNewProcessedMessages(processedMessages)
            analyticsManager.d(TAG, "doWork: Success")
            Result.success()
        } else {
            analyticsManager.d(TAG, "doWork: Failure")
            Result.failure()
        }

    }

    private suspend fun processMessage(messageFromAndroidDb: Message): RelayResult<Void> {
        // Store the message on local database in case uploading fails
        val smsInfoToInsert = messageFromAndroidDb.toSmsInfo()
        val idOfInsertedSmsInfo = smsInfoRepository.insertSmsInfo(smsInfoToInsert)

        val encryptedMessage = encryptionService.encrypt(
            gson.toJson(messageFromAndroidDb),
            currentUser.user.encryptionKey
        ) ?: run {
            analyticsManager.log(Level.ERROR, TAG, "Failed to encrypt message")
            return RelayResult.Failure()
        }

        // Push new message to the cloud database
        val result = apiInterface.postMessage(MessageEntry(
                "",
                currentUser.user.id,
                System.currentTimeMillis().toString(),
                encryptedMessage,
        ))
        // Update the sync status
        messageFromAndroidDb.syncStatus = result
        // Update the sync status in the local DB as well
        smsInfoRepository.updateSmsInfo(smsInfoToInsert.copy(
            id = idOfInsertedSmsInfo,
            syncStatus = result
        ))

        return result
    }

    companion object {

        val TAG: String = UploadNewMessagesWorker::class.java.simpleName

        private val constraints: Constraints by lazy {
            Constraints.Builder()
                .addContentUriTrigger(Sms.CONTENT_URI, true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setTriggerContentMaxDelay(3, TimeUnit.SECONDS)
                .setTriggerContentUpdateDelay(3, TimeUnit.SECONDS)
                .build()
        }

        fun doEnqueueWorkerToUploadNewMessages(workManager: WorkManager) {
            workManager.enqueueUniqueWork(
                TAG,
                // NOTE: APPEND isn't triggering the worker at all
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<UploadNewMessagesWorker>()
                    .addTag(TAG)
                    .setConstraints(constraints)
                    .build()
            )
        }

    }
}