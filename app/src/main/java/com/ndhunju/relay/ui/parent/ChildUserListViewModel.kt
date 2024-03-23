package com.ndhunju.relay.ui.parent

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.checkIfPostNotificationPermissionGranted
import com.ndhunju.relay.util.extensions.asState
import com.ndhunju.relay.util.worker.SyncChildMessagesWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildUserListViewModel(
    apiInterface: ApiInterface,
    private val workManager: WorkManager,
    private val currentUser: CurrentUser,
    private val application: Application,
    private val simpleKeyValuePersistService: SimpleKeyValuePersistService,
): ViewModel() {

    private val keyNotificationPermissionDeniedTime = "notificationPermissionDeniedTimeStamp"

    private val _childUsers = MutableStateFlow<List<Child>>(emptyList())
    val childUsers = _childUsers.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    private val _showPostNotificationPermissionDialog = mutableStateOf(false)
    val showPostNotificationPermissionDialog = _showPostNotificationPermissionDialog.asState()

    var doOpenMessagesFromChildFragment: ((Child) -> Unit)? = null
    var doOpenAddChildEncryptionKeyFromQrCodeFragment: ((Child) -> Unit)? = null
    var doRequestNotificationPermission: (() -> Unit)? = null

    /**
     * User clicked on [childUsers]
     */
    fun onClickChildUser(childUser: Child) {
        doOpenMessagesFromChildFragment?.invoke(childUser)
    }

    fun onClickAddChildKey(child: Child) {
        doOpenAddChildEncryptionKeyFromQrCodeFragment?.invoke(child)
    }
    
    fun onDeniedNotificationPermission() {
        _showPostNotificationPermissionDialog.value = false
        // Save the timestamp of when was notification permission was denied
        // This way, we can determine when to ask for this again next time
        viewModelScope.launch {
            simpleKeyValuePersistService.save(
                keyNotificationPermissionDeniedTime,
                System.currentTimeMillis().toString()
            )
        }
    }

    fun onClickAllowNotificationDialogButton() {
        // Dismiss the dialog when any button is clicked
        _showPostNotificationPermissionDialog.value = false
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Show pair child users that were saved previously
            _childUsers.value = currentUser.user.getChildUsers().map { childUser ->
                Child(childUser.id, childUser.email ?: "", childUser.encryptionKey)
            }

            // If no child users are saved before,
            // show spinner until network call to get them finishes
            _showProgress.value = currentUser.user.getChildUsers().isEmpty()

            // Fetch child users in case there are new ones since last time
            val result = withContext(Dispatchers.IO) {
                apiInterface.getChildUsers(
                    currentUser.user.id
                )
            }

            when (result) {
                is Result.Failure -> _showProgress.value = false
                is Result.Pending ->  _showProgress.value = true
                is Result.Success -> {
                    val newChildUserWithoutEncKey = result.data as List<Child>

                    // Persist it locally
                    val childUsers = newChildUserWithoutEncKey.map { User(it.id, it.email) }
                    currentUser.user.updateChildUsersWithoutLosingEncryptionKey(childUsers)

                    // Update UI
                    _showProgress.value = false
                    _childUsers.value = currentUser.user.getChildUsers().map {
                        Child(it.id, it.email ?: "", it.encryptionKey)
                    }

                    doSyncChildMessagesFromServer()
                }
            }

            showAllowNotificationDialogIfNeeded()
        }
    }

    fun invalidateChildUsers() {
        // Update child users in UI
        _childUsers.value = currentUser.user.getChildUsers().map { childUser ->
            Child(childUser.id, childUser.email ?: "", childUser.encryptionKey)
        }
    }

    private fun doSyncChildMessagesFromServer() {
        workManager.enqueue(OneTimeWorkRequestBuilder<SyncChildMessagesWorker>().build())
    }
}