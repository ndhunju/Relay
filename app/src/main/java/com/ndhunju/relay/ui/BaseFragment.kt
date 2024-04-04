package com.ndhunju.relay.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.util.extensions.setTextAndVisibility
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base fragment to isolate features common to all fragments
 */
open class BaseFragment: Fragment() {

    /**
     * Dagger will provide an instance of [AppStateBroadcastService] from the graph
     */
    @Inject lateinit var appStateBroadcastService: AppStateBroadcastService
    @Inject lateinit var analyticsProvider: AnalyticsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFields()
        checkForDeviceOnlineState()
    }

    private fun injectFields() {
        (context?.applicationContext as? RelayApplication)?.appComponent?.inject(this)
    }

    private fun checkForDeviceOnlineState() {
        appStateBroadcastService.isDeviceOnline.observe(this) { isOnline ->
            lifecycleScope.launch {
                val messageToShow = if (isOnline) null else getString(R.string.alert_offline_body)
                showCriticalMessageBar(messageToShow)
            }
        }
    }

    private var totalAttempts = 0

    /**
     * Shows critical message in a visually hard to miss way
     */
    open suspend fun showCriticalMessageBar(message: String?) {
        val textView = view?.findViewById<TextView>(R.id.critical_message_text_view)
        // Sometimes, the view is not rendered when this fun is called
        // So check again after a second to 3 more times
        if (textView == null && totalAttempts <= 3) {
            totalAttempts++
            delay(1000)
            showCriticalMessageBar(message)
            return
        }
        // Reset totalAttempts
        totalAttempts = 0
        // Show the message
        textView?.setTextAndVisibility(message)
    }

}