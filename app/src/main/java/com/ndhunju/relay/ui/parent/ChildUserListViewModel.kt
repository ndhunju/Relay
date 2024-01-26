package com.ndhunju.relay.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.worker.SyncChildMessagesWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChildUserListViewModel(
    apiInterface: ApiInterface,
    private val workManager: WorkManager,
    private val currentUser: CurrentUser,
    userSettingsPersistService: UserSettingsPersistService,
): ViewModel() {

    private val _childUsers = MutableStateFlow<List<Child>>(emptyList())
    val childUsers = _childUsers.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    var doOpenMessagesFromChildFragment: ((Child) -> Unit)? = null

    /**
     * User clicked on [childUsers]
     */
    fun onClickChildUser(childUser: Child) {
        doOpenMessagesFromChildFragment?.invoke(childUser)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _showProgress.value = true
            // TODO: Nikesh - Check if the child users are already stored and is not stale
            val result = apiInterface.fetchChildUsers(
                currentUser.user.id
            )

            when (result) {
                is Result.Failure -> {
                    _showProgress.value = false
                }
                Result.Pending -> {
                    _showProgress.value = true
                }
                is Result.Success -> {
                    _showProgress.value = false
                    _childUsers.value = result.data as List<Child>

                    // Persist it locally
                    currentUser.user = currentUser.user.copy(
                        childUserIds = _childUsers.value.map { it.id },
                        childUserEmails = _childUsers.value.map { it.email }
                    )

                    userSettingsPersistService.save(currentUser.user)
                    doSyncChildMessagesFromServer()
                }
            }
        }
    }

    private fun doSyncChildMessagesFromServer() {
        workManager.enqueue(OneTimeWorkRequestBuilder<SyncChildMessagesWorker>().build())
    }
}