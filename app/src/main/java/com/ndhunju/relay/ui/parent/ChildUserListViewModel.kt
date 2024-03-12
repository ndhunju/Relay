package com.ndhunju.relay.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.worker.SyncChildMessagesWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildUserListViewModel(
    apiInterface: ApiInterface,
    private val workManager: WorkManager,
    private val currentUser: CurrentUser
): ViewModel() {

    private val _childUsers = MutableStateFlow<List<Child>>(emptyList())
    val childUsers = _childUsers.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    var doOpenMessagesFromChildFragment: ((Child) -> Unit)? = null
    var doOpenAddChildEncryptionKeyFromQrCodeFragment: ((Child) -> Unit)? = null

    /**
     * User clicked on [childUsers]
     */
    fun onClickChildUser(childUser: Child) {
        doOpenMessagesFromChildFragment?.invoke(childUser)
    }

    fun onClickAddChildKey(child: Child) {
        doOpenAddChildEncryptionKeyFromQrCodeFragment?.invoke(child)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Show pair child users that were saved previously
            _childUsers.value = currentUser.user.getChildUsers().map { childUser ->
                Child(childUser.id, childUser.email ?: "")
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
                    _showProgress.value = false
                    _childUsers.value = result.data as List<Child>

                    // Persist it locally
                    val childUsers = childUsers.value.map { User(it.id, it.email) }
                    currentUser.user.updateChildUsers(childUsers)
                    doSyncChildMessagesFromServer()
                }
            }
        }
    }

    private fun doSyncChildMessagesFromServer() {
        workManager.enqueue(OneTimeWorkRequestBuilder<SyncChildMessagesWorker>().build())
    }
}