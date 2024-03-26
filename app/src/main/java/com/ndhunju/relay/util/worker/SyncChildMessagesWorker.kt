package com.ndhunju.relay.util.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result.*
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.service.EncryptionService
import com.ndhunju.relay.service.NotificationManager
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import java.util.UUID
import com.ndhunju.relay.api.Result as RelayResult

/**
 * This class is responsible for fetching messaging from child user and storing it locally
 */
class SyncChildMessagesWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val appComponent: AppComponent by lazy {
        (applicationContext as RelayApplication).appComponent
    }

    private val apiInterface: ApiInterface by lazy {
        appComponent.apiInterface()
    }

    private val analyticsProvider: AnalyticsProvider by lazy {
        appComponent.analyticsProvider()
    }

    private val currentUser: CurrentUser by lazy {
        appComponent.currentUser()
    }

    private val encryptionService: EncryptionService by lazy {
        appComponent.encryptionService()
    }

    private val notificationManager: NotificationManager by lazy {
        appComponent.notificationManager()
    }

    private val gson: Gson by lazy {
        appComponent.gson()
    }

    override suspend fun doWork(): Result {
        val workResult = withContext(Dispatchers.IO) {
            var result: RelayResult<Any> = Success()
            currentUser.user.getChildUsers().forEach { childUser ->
                result += processMessagesForChild(childUser)
            }

            return@withContext if (result is Success) Result.success() else returnFailure()
        }

        return workResult
    }

    private suspend fun processMessagesForChild(childUser: User): RelayResult<Any> {
        when (val result = apiInterface.getMessagesFromChildUser(childUser.id)) {
            is Failure -> return Failure(result.throwable)
            is Pending -> {}
            is Success -> {
                val messageCollectionList = result.data ?: return Failure()
                val password = currentUser.user.getEncryptionKey(childUser.id)
                val childSmsInfoList = messageCollectionList.map {
                    val decryptedPayLoad = encryptionService.decrypt(it.payLoad, password)

                    if (decryptedPayLoad == null || password == null) {
                        currentUser.user.invalidateEncryptionKeyOfChild(childUser.getPublicIdentifier())
                        notificationManager.notifyCriticalMessage(applicationContext.getString(
                            R.string.notification_invalid_encryption_key,
                            childUser.getPublicIdentifier()
                        ))
                        // Even though, password is wrong, the operation is a success since
                        // we invalidate the encryption key for that child and let user know
                        return Success()
                    }

                    gson.fromJson(decryptedPayLoad, ChildSmsInfo::class.java).apply {
                        childUserId = childUser.id
                        idInServerDb = it.idInServer
                    }
                }

                val isSuccess = insertIntoLocalRepository(childSmsInfoList)
                // Tell back end that the messages have been saved locally
                apiInterface.postDidSaveFetchedMessages(childSmsInfoList)
                return if (isSuccess) Success() else Failure()
            }

        }

        return Failure()
    }

    /**
     * Insert [childUserIdToChildSmsInfoList] to local repository
     */
    private suspend fun insertIntoLocalRepository(
        childUserIdToChildSmsInfoList: List<ChildSmsInfo>
    ): Boolean {
        try {
            val childSmsInfoRepository = appComponent.childSmsInfoRepository()
            for (childSmsInfo in childUserIdToChildSmsInfoList) {
                val insertedRowId = childSmsInfoRepository.insert(childSmsInfo)
                if (insertedRowId < 0) {
                    throw RuntimeException(
                        "Failed to insert childSmsInfo with body ${childSmsInfo.body}"
                    )
                }
            }

            return true
        } catch (ex: Exception) {
            analyticsProvider.logEvent("didFailToInsertIntoLocalRepo", ex.message)
            return false
        }
    }

    private fun returnFailure(throwable: Throwable? = null): Result {
        analyticsProvider.logEvent(
            "didFailToSyncChildMessage",
            throwable?.message
        )
        return Result.failure()
    }

    companion object {

        val TAG: String = SyncChildMessagesWorker::class.java.simpleName

        fun doSyncChildMessagesFromServer(workManager: WorkManager): UUID {
            val workRequest = OneTimeWorkRequestBuilder<SyncChildMessagesWorker>()
                .addTag(TAG)
                .build()
            workManager.enqueue(workRequest)
            return workRequest.id
        }
    }
}