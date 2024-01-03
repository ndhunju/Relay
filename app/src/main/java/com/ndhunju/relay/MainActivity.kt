package com.ndhunju.relay

import android.Manifest.permission.READ_SMS
import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.areNeededPermissionGranted
import com.ndhunju.relay.util.checkIfPermissionGranted
import com.ndhunju.relay.util.readSms
import com.ndhunju.relay.util.requestPermission

class MainActivity : ComponentActivity() {

    // Member Variables
    private val viewModel = RelaySmsViewModel()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (areNeededPermissionGranted(permissions)) {
            // All permissions granted
            viewModel.state.value.messages = readSms(contentResolver)
            // Reset this value in case it was set to true earlier
            viewModel.state.value.showErrorMessageForPermissionDenied = false
        } else {
            // Permissions denied
            viewModel.state.value.showErrorMessageForPermissionDenied = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RelaySmsApp(viewModel)
                }
            }
        }

        viewModel.onClickGrantPermission = {
            requestPermission(requestPermissionLauncher)
        }

        // Check if needed permissions are granted
        if (checkIfPermissionGranted(this)) {
            viewModel.state.value.messages = readSms(contentResolver)
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
}