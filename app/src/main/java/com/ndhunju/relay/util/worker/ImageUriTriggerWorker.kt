package com.ndhunju.relay.util.worker

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Source: https://github.com/sdex/WorkManager-Samples/blob/master/uri_trigger/src/main/java/dev/sdex/uritrigger/UriTriggerWorker.kt
 */
class ImageUriTriggerWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("UriTriggerWorker", inputData.toString())
        enqueue(applicationContext)
        return Result.success()
    }

    companion object {

        private val CONTENT_URI: Uri = Uri.parse("content://" + MediaStore.AUTHORITY + "/")

        fun enqueue(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(getWorkRequest())
        }

        private fun getWorkRequest(): WorkRequest {
            val builder = Constraints.Builder()
                .addContentUriTrigger(CONTENT_URI, false)
                .addContentUriTrigger(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true)
                .addContentUriTrigger(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true)
                .setTriggerContentMaxDelay(5, TimeUnit.SECONDS)
                .setTriggerContentUpdateDelay(5, TimeUnit.SECONDS)
            return OneTimeWorkRequestBuilder<ImageUriTriggerWorker>()
                .addTag("uri_trigger_worker")
                .setConstraints(builder.build())
                .build()
        }
    }
}