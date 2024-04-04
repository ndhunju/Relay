package com.ndhunju.relay.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.debug.DebugFragment
import com.ndhunju.relay.ui.messages.MessageThreadFragment
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.ui.pair.PairWithParentFragment
import com.ndhunju.relay.ui.pair.ShareEncryptionKeyWithQrCodeActivity
import com.ndhunju.relay.ui.parent.ChildUserListFragment

class MainActivity : BaseActivity() {

    // Member Variables
    private val viewModel: MainViewModel by viewModels { RelayViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTitle(getString(R.string.app_name))
        showSplashScreenUntilReady()
        bindNavigationCallbacks()
        // Add MessageThreadFragment immediately
        MessageThreadFragment.addToContent(supportFragmentManager)
    }

    private fun bindNavigationCallbacks() {

        viewModel.doOpenPairWithParentFragment = {
            PairWithParentFragment.addToContent(supportFragmentManager)
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
            MessagesFromFragment.addToContent(supportFragmentManager, message)
        }

        viewModel.doOpenAccountFragment = {
            AccountFragment.addToContent(supportFragmentManager)
        }

        viewModel.doOpenChildUserFragment = {
            ChildUserListFragment.addToContent(supportFragmentManager)
        }

        viewModel.doOpenDebugFragment = {
            DebugFragment.addToContent(supportFragmentManager)
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

    private fun checkForMinimumVersionCode() {
        lifecycleScope.launch {
            currentSettings.settings.collectLatest { settings ->
                val currentVersionCode = getAppVersionCode()
                if (currentVersionCode < settings.minimumVersionCode) {
                    showAppUpdateDialog(settings.androidAppLink)
                    analyticsProvider.logEvent("didShowAppUpdateDialog", "$currentVersionCode")
                }
            }
        }
    }

    private fun showAppUpdateDialog(androidAppLink: String?) {
        addContentView(
            ComposeView(this@MainActivity).apply { setContent { AppUpdateDialog {
                startActivityForUrl(androidAppLink)
            } } },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

}