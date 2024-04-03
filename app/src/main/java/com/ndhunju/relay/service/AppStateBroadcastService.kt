package com.ndhunju.relay.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ndhunju.relay.ui.messages.Message
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
     * Stores new processed [Message]s.
     * UI can make use of this to update their state.
     */
    val newSyncedMessages: LiveData<List<Message>>

    /**
     * Stores the time stamp of when the new messages where received
     */
    val newMessagesReceivedTime: LiveData<Long>

    /**
     * Updates [isUserSignedIn] if different and notifies observer
     */
    fun updateIsUserSignedIn(newValue: Boolean)

    /**
     * Updates [isDeviceOnline] if different and notifies observer
     */
    fun updateIsDeviceOnline(newValue: Boolean)

    /**
     * Updates [newSyncedMessages] and notifies observer
     */
    fun updateNewSyncedMessages(newValue: List<Message>)

    /**
     * Updates [newMessagesReceivedTime] and notifies observer
     */
    fun updateNewMessagesReceivedTime(newTime: Long)
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

    private val _newSyncedMessages = MutableLiveData<List<Message>>()
    override val newSyncedMessages = _newSyncedMessages

    private val _newMessagesReceivedTime = MutableLiveData<Long>()
    override val newMessagesReceivedTime = _newMessagesReceivedTime

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

    override fun updateNewSyncedMessages(newValue: List<Message>) {
        // Log who made the update request
        Log.d(TAG, "updateNewSyncedMessages: ${Throwable().stackTrace.first()}")
        _newSyncedMessages.postValue(newValue)
    }

    override fun updateNewMessagesReceivedTime(newTime: Long) {
        // Log who made the update request
        Log.d(TAG, "updateNewMessagesReceivedTime: ${Throwable().stackTrace.first()}")
        _newMessagesReceivedTime.postValue(newTime)
    }

    companion object {
        val TAG: String = AppStateBroadcastServiceImpl::class.java.simpleName
    }

}