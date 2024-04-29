package com.ndhunju.relay.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.ui.custom.AppUpdateDialog
import com.ndhunju.relay.ui.custom.MessageAlertDialog
import com.ndhunju.relay.util.CurrentSettings
import com.ndhunju.relay.util.extensions.getAppVersionCode
import com.ndhunju.relay.util.extensions.startActivityForUrl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity: FragmentActivity() {

    /**
     * Dagger will provide an instance of [AppStateBroadcastService] from the graph
     */
    @Inject lateinit var appStateBroadcastService: AppStateBroadcastService
    @Inject lateinit var analyticsProvider: AnalyticsProvider
    @Inject lateinit var currentSettings: CurrentSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFields()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        checkForMinimumVersionCode()
    }

    /**
     * Injects fields annotated with @Inject
     */
    private fun injectFields() {
        // Make Dagger instantiate @Inject fields in this activity
        (applicationContext as RelayApplication).appComponent.inject(this)
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
            ComposeView(this).apply { setContent { AppUpdateDialog {
                startActivityForUrl(androidAppLink)
            } } },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    /**
     * Convenient method to show messages on screen that can be dismissed
     */
    fun showDialog(message: String) {
        addContentView(
            ComposeView(this).apply { setContent {
                MessageAlertDialog(
                    message,
                    onClickDialogBtnOk = { (parent as ViewGroup).removeView(this) },
                    onClickDialogBtnCancel = { (parent as ViewGroup).removeView(this) }
                )
            } },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun showDialog(content: @Composable () -> Unit): View {
        val view: View = ComposeView(this).apply { setContent { content() } }
        addContentView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        return view
    }

}