package com.ndhunju.relay.util.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Implements [NetworkConnectionChecker] for Lollipop and below versions
 */
class LollipopNetworkConnectionChecker(context: Context): NetworkConnectionChecker(context) {

    override fun registerNetworkCallback() {
        context.registerReceiver(
            networkReceiver,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
    }

    override fun unregisterNetworkCallback() {
        context.unregisterReceiver(networkReceiver)
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkAndPostNetworkConnectivityState()
        }
    }

}