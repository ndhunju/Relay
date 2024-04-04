package com.ndhunju.relay.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.ui.custom.AppUpdateDialog
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
        checkForDeviceOnlineState()
        checkForMinimumVersionCode()
    }

    /**
     * Injects fields annotated with @Inject
     */
    private fun injectFields() {
        // Make Dagger instantiate @Inject fields in this activity
        (applicationContext as RelayApplication).appComponent.inject(this)
    }

    private fun checkForDeviceOnlineState() {
        appStateBroadcastService.isDeviceOnline.observe(this) { isOnline ->
            if (isOnline.not()) {
                Toast.makeText(
                    this,
                    getString(R.string.alert_offline_body),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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

}