package com.ndhunju.relay.util.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.api.Result.*
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * This class is responsible for fetching messaging from child user and storing it locally
 */
class SyncChildMessagesWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val apiInterface = (applicationContext as RelayApplication).appComponent.apiInterface()
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
                            val messages = result2.data as MutableMap<String, List<Message>>
                            Log.d("TAG", "messages: $messages")
                            // TODO: Nikesh Store in database
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
}