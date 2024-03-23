package com.ndhunju.relay.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.debug.DebugFragment
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.ui.pair.PairWithParentFragment
import com.ndhunju.relay.ui.pair.ShareEncryptionKeyWithQrCodeActivity
import com.ndhunju.relay.ui.parent.ChildUserListFragment
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.areSmsPermissionGranted
import com.ndhunju.relay.util.checkIfSmsPermissionsGranted
import com.ndhunju.relay.util.requestPermission

class MainActivity : BaseActivity() {

    // Member Variables
    private val viewModel: MainViewModel by viewModels { RelayViewModelFactory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (areSmsPermissionGranted(permissions)) {
            viewModel.onSmsPermissionGranted()
        } else {
            // Permissions denied
            viewModel.onSmsPermissionDenied()
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
                    MainScreen(viewModel)
                }
            }
        }

        showSplashScreenUntilReady()

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

        //viewModel.doOpenPairWithChild = {
        //    startActivity(Intent(
        //        this,
        //        PairWithChildByScanningQrCodeActivity::class.java
        //    ))
        //}

        viewModel.doOpenEncryptionKeyScreen = {
            startActivity(Intent(this, ShareEncryptionKeyWithQrCodeActivity::class.java))
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

        viewModel.doOpenDebugFragment = {
            supportFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    DebugFragment.newFragment(),
                    DebugFragment.TAG
                )
                .addToBackStack(DebugFragment.TAG)
                .commit()
        }

        // Check if SMS read and send permissions are granted
        if (checkIfSmsPermissionsGranted(this)) {
            viewModel.onSmsPermissionGranted()
        } else {
            // if (shouldShowRequestPermissionRationale(this, READ_SMS))
            // Always show the rationale
            viewModel.onSmsPermissionDenied()
        }
    }

    /**
     * Shows splash screen until the view model has finished loading data
     */
    private fun showSplashScreenUntilReady() {
        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check whether the initial data is ready.
                    return if (viewModel.showSplashScreen.value) {
                        // The data isn't ready.
                        // Suspend first draw which would hide splash screen
                        false
                    } else {
                        // The content is ready. Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    }
                }
            }
        )
    }

}