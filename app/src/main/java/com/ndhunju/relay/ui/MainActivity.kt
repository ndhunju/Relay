package com.ndhunju.relay.ui

import android.Manifest.permission.READ_SMS
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.fragment.app.FragmentActivity
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.ui.pair.PairWithParentFragment
import com.ndhunju.relay.ui.parent.ChildUserListFragment
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.areNeededPermissionGranted
import com.ndhunju.relay.util.checkIfPermissionGranted
import com.ndhunju.relay.util.requestPermission

class MainActivity : FragmentActivity() {

    // Member Variables
    val viewModel: MainViewModel by viewModels { RelayViewModelFactory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (areNeededPermissionGranted(permissions)) {
            viewModel.onAllPermissionGranted()
            // Create and register the SMS broadcast receiver
            createAndRegisterBroadcastReceiver()
        } else {
            // Permissions denied
            viewModel.state.value.showErrorMessageForPermissionDenied = true
        }
    }

    private lateinit var smsReceiver: BroadcastReceiver
    private var isRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }

        viewModel.setTitle(getString(R.string.app_name))

        viewModel.doRequestPermission = {
            requestPermission(requestPermissionLauncher)
        }

        viewModel.doOpenPairWithParentFragment = {
            supportFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    PairWithParentFragment.newInstance(),
                    PairWithParentFragment.TAG
                )
                .addToBackStack(PairWithParentFragment.TAG)
                .commit()
        }

        viewModel.doOpenMessageFromFragment = { message ->
            // Open MessagesFromFragment
            supportFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    MessagesFromFragment.newInstance(message.threadId, message.from),
                    MessagesFromFragment.TAG
                    )
                .addToBackStack(MessagesFromFragment.TAG)
                .commit()
        }

        viewModel.doOpenAccountFragment = {
            supportFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    AccountFragment.newInstance(),
                    AccountFragment.TAG
                )
                .addToBackStack(AccountFragment.TAG)
                .commit()
        }

        viewModel.doOpenChildUserFragment = {
            supportFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    ChildUserListFragment.newInstance(),
                    ChildUserListFragment.TAG
                )
                .addToBackStack(ChildUserListFragment.TAG)
                .commit()
        }

        // Check if needed permissions are granted
        if (checkIfPermissionGranted(this)) {
            viewModel.onAllPermissionGranted()
        } else {
            if (shouldShowRequestPermissionRationale(this, READ_SMS)) {
                // Show an explanation as to why the app needs read and send SMS permission
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.permission_rationale_sms_read_send))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        requestPermission(requestPermissionLauncher)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            } else {
                requestPermission(requestPermissionLauncher)
            }
        }
    }

    private fun createAndRegisterBroadcastReceiver() {
        if (isRegistered.not()) {
            smsReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {

                    if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                        for (smsMessage in smsMessages) {
                            //println("Received SMS: $messageBody")
                            viewModel.onNewSmsReceived(smsMessage)
                        }
                    }
                }
            }

            val intent = registerReceiver(
                smsReceiver,
                IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            )
            isRegistered = intent != null
        }
    }

    override fun onResume() {
        super.onResume()

        // Ensure permissions are granted before registering the receiver
        if (checkIfPermissionGranted(this)) {
            createAndRegisterBroadcastReceiver()
        }
    }

    override fun onPause() {
        super.onPause()

        if (isRegistered) {
            unregisterReceiver(smsReceiver)
        }
    }

}