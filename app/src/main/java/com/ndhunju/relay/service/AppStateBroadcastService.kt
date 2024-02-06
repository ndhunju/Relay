package com.ndhunju.relay.service

import android.util.Log
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides fields to observe app wide state,
 * and APIs to update those state
 */
interface AppStateBroadcastService {

    /**
     * True if [CurrentUser] is signed in
     */
    val isUserSignedIn: StateFlow<Boolean>

    /**
     * True if device has internet
     */
    val isDeviceOnline: StateFlow<Boolean>

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
    currentUser: CurrentUser
): AppStateBroadcastService {

    private val _isUserSignedIn = MutableStateFlow(currentUser.isUserSignedIn())
    override val isUserSignedIn = _isUserSignedIn.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    override val isDeviceOnline = _isOnline.asStateFlow()

    override fun updateIsUserSignedIn(newValue: Boolean) {
        if (newValue == _isUserSignedIn.value) return
        // Log who made the update request
        Log.d(TAG, "updateIsUserSignedIn: ${Throwable().stackTrace.first()}")
        _isUserSignedIn.value = newValue
    }

    override fun updateIsDeviceOnline(newValue: Boolean) {
        if (newValue == _isOnline.value) return
        // Log who made the update request
        Log.d(TAG, "updateIsDeviceOnline: ${Throwable().stackTrace.first()}")
        _isOnline.value = newValue
    }

    companion object {
        val TAG: String = AppStateBroadcastServiceImpl::class.java.simpleName
    }

}