package com.ndhunju.relay.util.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.Result.*
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.di.AppComponent
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
        val apiInterface = appComponent.apiInterface()
        return withContext(Dispatchers.IO) {
            var result: Result? = null
            val job = async {
                apiInterface.fetchMessagesFromChildUsers(
                    CurrentUser.user.childUserIds
                ).collect { result2 ->
                    when (result2) {
                        is Failure -> {
                            Log.d("TAG", "Failure: ${result2.throwable}")
                            result = Result.failure(Data.Builder().build())
                            return@collect
                        }

                        Pending -> {}
                        is Success -> {
                            insertIntoLocalRepository(
                                result2.data as MutableMap<String, List<ChildSmsInfo>>
                            )
                            result = Result.success()
                            return@collect
                        }
                    }
                }
            }

            job.await()
            return@withContext result ?: Result.failure()
        }
    }

    /**
     * Insert [childUserIdToChildSmsInfoList] to local repository
     */
    private suspend fun insertIntoLocalRepository(
        childUserIdToChildSmsInfoList: Map<String, List<ChildSmsInfo>>
    ): Boolean {
        try {
            val childSmsInfoRepository = appComponent.childSmsInfoRepository()
            for ((childUserId, childSmsInfoList) in childUserIdToChildSmsInfoList) {
                for (childSmsInfo in childSmsInfoList) {
                    val insertedRowId = childSmsInfoRepository.insert(
                        childSmsInfo
                    )

                    if (insertedRowId < 0) {
                        throw RuntimeException(
                            "Failed to insert childSmsInfo with body ${childSmsInfo.body}"
                        )
                    }
                }
            }

            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }
}