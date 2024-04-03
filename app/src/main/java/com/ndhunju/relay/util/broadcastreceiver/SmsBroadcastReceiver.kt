package com.ndhunju.relay.util.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.util.worker.NewMessageNotifierWorker
import com.ndhunju.relay.util.worker.UploadNewMessagesWorker

/**
 * This [BroadcastReceiver] instigates the process of uploading new messages to the server
 */
object SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            doEnqueueWorkerToUploadNewMessages(context)
            processNewSmsMessages(intent)
        }
    }

    private fun doEnqueueWorkerToUploadNewMessages(context: Context) {
        val appComponent = (context.applicationContext as RelayApplication).appComponent
        NewMessageNotifierWorker.doEnqueueWorker(appComponent.workManager())
        UploadNewMessagesWorker.doEnqueueWorkerToUploadNewMessages(appComponent.workManager())
    }

    private fun processNewSmsMessages(intent: Intent) {
//        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
//        for (smsMessage in smsMessages) {
//            println("Received SMS: ${smsMessage.messageBody}")
//        }
    }
}