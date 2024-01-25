package com.ndhunju.relay.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChildUserListViewModel(
    apiInterface: ApiInterface,
    parentUserId: String,
): ViewModel() {

    private val _childUsers = MutableStateFlow<List<Child>>(emptyList())
    val childUsers = _childUsers.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    /**
     * User click on [childUsers]
     */
    fun onClickChildUser(childUser: Child) {

    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            apiInterface.fetchChildUsers(parentUserId).collect { result ->
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
                    }
                }
            }
        }
    }
}