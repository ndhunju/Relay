package com.ndhunju.relay.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.util.CurrentSettings
import com.ndhunju.relay.util.extensions.getAppVersionCode
import com.ndhunju.relay.util.extensions.openIfLink
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
        checkForDeviceOnlineState()
        checkForMinimumVersionCode()
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
                    // Show dialog to update the app
                    AlertDialog.Builder(this@BaseActivity)
                        .setTitle(getString(R.string.screen_app_update_title))
                        .setMessage(getString(R.string.screen_app_update_body))
                        .setCancelable(false)
                        .setPositiveButton(
                            getString(R.string.screen_app_update_positive_btn),
                            null
                        ).show().apply {
                            setOnShowListener {
                                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                    openIfLink(settings.androidAppLink)
                                    //dismiss() Don't dismiss until the app is updated
                                }
                            }
                        }

                    analyticsProvider.logEvent("didShowAppUpdateDialog", "$currentVersionCode")
                }
            }
        }
    }

}