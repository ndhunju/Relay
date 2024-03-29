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
       return checkIfDeviceHasInternet(connectivityManager)
    }

    /**
     * Checks Network Connectivity state and posts it to observers
     */
    fun checkAndPostNetworkConnectivityState() {
        postValue(checkIfDeviceHasInternet())
    }

    companion object {

        fun checkIfDeviceHasInternet(context: Context): Boolean {
            return checkIfDeviceHasInternet(
                ContextCompat.getSystemService(
                    context,
                    ConnectivityManager::class.java
                )
            )
        }

        private fun checkIfDeviceHasInternet(connectivityManager: ConnectivityManager?): Boolean {

            val capabilities = connectivityManager?.getNetworkCapabilities(
                connectivityManager.activeNetwork
            ) ?: return false

            // isValidated is false on emulator
            //val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            //val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            //Log.d("TAG", "checkIfDeviceHasInternet: hasInternet=$hasInternet;isValidated=$isValidated")
            //Throwable().printStackTrace()
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}
