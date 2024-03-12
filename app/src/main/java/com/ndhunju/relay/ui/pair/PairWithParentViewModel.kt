package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.UserNotFoundException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PairWithParentViewModel(
    private val apiInterface: ApiInterface,
    private val currentChildUser: CurrentUser,
): ViewModel() {

    private val _parentEmailAddress = MutableStateFlow(
        currentChildUser.user.getParentEmails().firstOrNull() ?: ""
    )
    val parentEmailAddress = _parentEmailAddress.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    private val _errorMsgResId = MutableStateFlow<Int?>(null)
    val errorMsgResId = _errorMsgResId.asStateFlow()

    private val _isPaired = MutableStateFlow(evaluateIsPaired())
    val isPaired = _isPaired.asStateFlow()

    private val _pairedUserEmailList = MutableStateFlow(currentChildUser.user.getParentEmails())
    val pairedUserEmailList = _pairedUserEmailList.asStateFlow()

    fun onParentEmailAddressChanged(newValue: String) {
        _parentEmailAddress.value = newValue
        _isPaired.value = evaluateIsPaired()
    }

    fun onClickPair() {
        // Check if user has already paired with 3 users
        if (currentChildUser.user.getParentUsers().size >= 3) {
            _errorMsgResId.value = R.string.pair_screen_max_limit_reached
            return
        }

        viewModelScope.launch {
            // TODO: Handle the scenario where "Unpair" button text is shown
            _showProgress.value = true
            val result = apiInterface.postPairWithParent(
                currentChildUser.user.id,
                parentEmailAddress.value
            )

            when (result) {
                is Result.Failure -> {
                    _showProgress.value = false
                    _isPaired.value = evaluateIsPaired()
                    if (result.throwable is UserNotFoundException) {
                        _errorMsgResId.value = R.string.pair_screen_user_email_not_found
                    }
                    else {
                        _errorMsgResId.value = R.string.pair_screen_pair_failed
                    }
                }

                is Result.Pending -> _showProgress.value = true
                is Result.Success -> {
                    // Persist the value
                    val parentUserId = result.data as String

                    currentChildUser.user.addParentUser(User(
                        id = parentUserId,
                        email = _parentEmailAddress.value)
                    )

                    //userSettingsPersistService.save(currentChildUser.user)

                    // Update the UI
                    _showProgress.value = false
                    _errorMsgResId.value = null
                    _isPaired.value = evaluateIsPaired()
                    _pairedUserEmailList.value = currentChildUser.user.getParentEmails()
                }
            }
        }
    }

    /**
     * Evaluates value of [_isPaired] property
     */
    private fun evaluateIsPaired(): Boolean {
        // Return true if _parentEmailAddress.value matches with
        // any item in currentChildUser.user.parentUserEmails
        currentChildUser.user.getParentEmails().forEach { parentUserEmail ->
            if (parentUserEmail == _parentEmailAddress.value) {
                return true
            }
        }
        return false
    }

}