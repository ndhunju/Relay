package com.ndhunju.relay.util.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Implements [NetworkConnectionChecker] for Nougat and above versions
 */
class NougatNetworkConnectionChecker(context: Context) : NetworkConnectionChecker(context) {

    override fun registerNetworkCallback() {
        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .build(),
            networkCallback
        )
    }

    override fun unregisterNetworkCallback() {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            checkAndPostNetworkConnectivityState()
        }

        override fun onLost(network: Network) {
            checkAndPostNetworkConnectivityState()
        }
    }

}