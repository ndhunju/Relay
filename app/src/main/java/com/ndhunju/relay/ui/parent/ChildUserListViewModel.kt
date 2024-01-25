package com.ndhunju.relay.ui.parent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChildUserListViewModel(
    apiInterface: ApiInterface,
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
            apiInterface.fetchChildUsers(currentUser.user.id).collect { result ->
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

                        // TODO: Nikesh - Move this logic to a Service?
                        // Also fetch messages from all child
                        apiInterface.fetchMessagesFromChildUsers(
                            currentUser.user.childUserIds
                        ).collect { result2 ->
                            when (result2) {
                                is Result.Failure -> {
                                    Log.d("TAG", "Failure: ${result2.throwable}")
                                }
                                Result.Pending -> {}
                                is Result.Success -> {
                                    val messages = result2.data as MutableMap<String, List<Message>>
                                    Log.d("TAG", "messages: $messages")
                                    // Store in database

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}