package com.ndhunju.relay.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.connectivity.NetworkConnectionChecker

/**
 * Provides fields to observe app wide state,
 * and APIs to update those state
 */
interface AppStateBroadcastService {

    /**
     * True if [CurrentUser] is signed in
     */
    val isUserSignedIn: LiveData<Boolean>

    /**
     * True if device has internet
     */
    val isDeviceOnline: LiveData<Boolean>

    /**
     * Updates [isUserSignedIn] if different and notifies observer
     */
    fun updateIsUserSignedIn(newValue: Boolean)

    /**
     * Updates [isDeviceOnline] if different and notifies observer
     */
    fun updateIsDeviceOnline(newValue: Boolean)
}


/**
 * Simple implementation of [AppStateBroadcastService]
 */
class AppStateBroadcastServiceImpl(
    networkConnectionChecker: NetworkConnectionChecker,
    currentUser: CurrentUser
): AppStateBroadcastService {

    private val _isUserSignedIn = MutableLiveData(currentUser.isUserSignedIn())
    override val isUserSignedIn = _isUserSignedIn

    private val _isOnline = networkConnectionChecker
    override val isDeviceOnline = _isOnline

    override fun updateIsUserSignedIn(newValue: Boolean) {
        if (newValue == _isUserSignedIn.value) return
        // Log who made the update request
        Log.d(TAG, "updateIsUserSignedIn: ${Throwable().stackTrace.first()}")
        _isUserSignedIn.postValue(newValue)
    }

    override fun updateIsDeviceOnline(newValue: Boolean) {
        if (newValue == _isOnline.value) return
        // Log who made the update request
        Log.d(TAG, "updateIsDeviceOnline: ${Throwable().stackTrace.first()}")
        _isOnline.postValue(newValue)
    }

    companion object {
        val TAG: String = AppStateBroadcastServiceImpl::class.java.simpleName
    }

}