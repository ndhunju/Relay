package com.ndhunju.relay.ui

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import javax.inject.Inject

abstract class BaseActivity: FragmentActivity() {

    /**
     * Dagger will provide an instance of [AppStateBroadcastService] from the graph
     */
    @Inject lateinit var appStateBroadcastService: AppStateBroadcastService
    @Inject lateinit var analyticsManager: AnalyticsProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFields()
        observeDeviceOnlineState()
    }

    private fun observeDeviceOnlineState() {
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

}