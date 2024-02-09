package com.ndhunju.relay.util.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.util.worker.UploadNewMessagesWorker

/**
 * This [BroadcastReceiver] instigates the process of uploading new messages to the server
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            doEnqueueWorkerToUploadNewMessages(context)
            processNewSmsMessages(intent)
        }
    }

    private fun doEnqueueWorkerToUploadNewMessages(context: Context) {
        ((context.applicationContext) as RelayApplication).appComponent.workManager().enqueue(
            OneTimeWorkRequestBuilder<UploadNewMessagesWorker>()
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
            )
    }

    private fun processNewSmsMessages(intent: Intent) {
//        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
//        for (smsMessage in smsMessages) {
//            println("Received SMS: ${smsMessage.messageBody}")
//        }
    }
}