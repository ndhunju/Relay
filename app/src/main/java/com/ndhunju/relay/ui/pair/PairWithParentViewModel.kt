package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.EmailNotFoundException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PairWithParentViewModel(
    private val apiInterface: ApiInterface,
    private val currentChildUser: CurrentUser,
    private val userSettingsPersistService: UserSettingsPersistService,
): ViewModel() {

    private val _parentEmailAddress = MutableStateFlow(
        currentChildUser.user.parentUserEmail ?: ""
    )
    val parentEmailAddress = _parentEmailAddress.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    private val _errorMsgResId = MutableStateFlow<Int?>(null)
    val errorMsgResId = _errorMsgResId.asStateFlow()

    private val _isPaired = MutableStateFlow(evaluateIsPaired())
    val isPaired = _isPaired.asStateFlow()

    fun onParentEmailAddressChanged(newValue: String) {
        _parentEmailAddress.value = newValue
        _isPaired.value = evaluateIsPaired()
    }

    fun onClickPair() {
        viewModelScope.launch {
            // TODO: Handle the scenario where "Unpair" button text is shown
            apiInterface.pairWithParent(currentChildUser.user.id, parentEmailAddress.value)
                .collect { result ->
                    when (result) {
                        is Result.Failure -> {
                            _showProgress.value = false
                            _isPaired.value = false
                            if (result.throwable is EmailNotFoundException) {
                                _errorMsgResId.value = R.string.pair_screen_user_email_not_found
                            }
                            else {
                                _errorMsgResId.value = R.string.pair_screen_pair_failed
                            }
                        }

                        Result.Pending -> _showProgress.value = true
                        is Result.Success -> {
                            _showProgress.value = false
                            _isPaired.value = true
                            _errorMsgResId.value = null

                            // Persist the value
                            val parentUserId = result.data as String
                            currentChildUser.user = currentChildUser.user.copy(
                                parentUserId = parentUserId,
                                parentUserEmail = _parentEmailAddress.value
                            )
                            userSettingsPersistService.save(currentChildUser.user)
                        }
                    }
            }
        }
    }

    /**
     * Evaluates value of [_isPaired] property
     */
    private fun evaluateIsPaired(): Boolean {
        return _parentEmailAddress.value == currentChildUser.user.parentUserEmail
    }

}