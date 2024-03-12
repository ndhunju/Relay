package com.ndhunju.relay.util.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result.*
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.service.AnalyticsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

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

    private val analyticsManager: AnalyticsManager by lazy {
        appComponent.analyticsManager()
    }

    override suspend fun doWork(): Result {
        val workResult = withContext(Dispatchers.IO) {
            val result = apiInterface.getMessagesFromChildUsers(
                appComponent.currentUser().user.childUsers.map { it.id }
            )

            when (result) {
                is Failure -> {
                    return@withContext returnFailure(result.throwable)
                }

                Pending -> {}
                is Success -> {
                    val childSmsInfoList = result.data as List<ChildSmsInfo>
                    val isSuccess = insertIntoLocalRepository(childSmsInfoList)
                    // Tell back end that the messages have been saved locally
                    apiInterface.postDidSaveFetchedMessages(childSmsInfoList)
                    return@withContext if (isSuccess) Result.success() else returnFailure()
                }
            }

            return@withContext returnFailure()
        }

        return workResult
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
            analyticsManager.logEvent("didFailToInsertIntoLocalRepo", ex.message)
            return false
        }
    }

    private suspend fun returnFailure(throwable: Throwable? = null): Result {
        analyticsManager.logEvent(
            "didFailToSyncChildMessage",
            throwable?.message
        )
        return Result.failure()
    }
}