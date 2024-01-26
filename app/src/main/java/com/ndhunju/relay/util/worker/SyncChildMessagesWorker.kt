package com.ndhunju.relay.util.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.Result.*
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.util.CurrentUser
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

    override suspend fun doWork(): Result {
        val workResult = withContext(Dispatchers.IO) {
            val result = appComponent.apiInterface().fetchMessagesFromChildUsers(
                CurrentUser.user.childUserIds
            )

            when (result) {
                is Failure -> {
                    //Log.d("TAG", "Failure: ${result.throwable}")
                    return@withContext Result.failure()
                }

                Pending -> {}
                is Success -> {
                    val isSuccess = insertIntoLocalRepository(result.data as List<ChildSmsInfo>)
                    return@withContext if (isSuccess) Result.success() else Result.failure()
                }
            }

            return@withContext Result.failure()
        }

        Log.d("TAG", "doWork: finished")
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
            ex.printStackTrace()
            return false
        }
    }
}