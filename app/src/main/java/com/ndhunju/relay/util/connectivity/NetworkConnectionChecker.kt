package com.ndhunju.relay.util.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

/**
 * Checks and notifies active observers whenever the network connection changes
 */
abstract class NetworkConnectionChecker(val context: Context) : MutableLiveData<Boolean>() {

    open var connectivityManager = ContextCompat.getSystemService(
        context,
        ConnectivityManager::class.java
    )

    override fun onActive() {
        super.onActive()
        checkAndPostNetworkConnectivityState()
        registerNetworkCallback()
    }

    override fun onInactive() {
        super.onInactive()
        unregisterNetworkCallback()
    }

    /**
     * Must register to listen to network connectivity updates from the system
     */
    abstract fun registerNetworkCallback()

    /**
     * Must unregister to listen to network connectivity updates from the system
     */
    abstract fun unregisterNetworkCallback()

    /**
     * Convenient method to check if the device has internet connection or not
     */
    private fun checkIfDeviceHasInternet(): Boolean {

        val capabilities = connectivityManager?.getNetworkCapabilities(
            connectivityManager?.activeNetwork
        ) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Checks Network Connectivity state and posts it to observers
     */
    fun checkAndPostNetworkConnectivityState() {
        postValue(checkIfDeviceHasInternet())
    }
}
