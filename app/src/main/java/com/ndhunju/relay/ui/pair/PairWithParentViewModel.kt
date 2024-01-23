package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.util.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PairWithParentViewModel(
    private val apiInterface: ApiInterface,
    private val childUser: User
): ViewModel() {

    private val _parentEmailAddress = MutableStateFlow("")
    val parentEmailAddress = _parentEmailAddress.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    val onParentEmailAddressChanged: (String) -> Unit = {
        _parentEmailAddress.value = it
    }

    val onClickPair: () -> Unit = {
        viewModelScope.launch {
            apiInterface.pairWithParent(childUser.id, parentEmailAddress.value).collect { result ->
                when (result) {
                    is Result.Failure -> {
                        _showProgress.value = false
                        // TODO: Post error to the UI
                    }
                    Result.Pending -> _showProgress.value = true
                    is Result.Success -> {
                        _showProgress.value = false
                        // TODO: Post successful pair message to the UI
                    }
                }
            }
        }
    }

}