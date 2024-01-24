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
        currentChildUser.user.parentUserEmails.firstOrNull() ?: ""
    )
    val parentEmailAddress = _parentEmailAddress.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    private val _errorMsgResId = MutableStateFlow<Int?>(null)
    val errorMsgResId = _errorMsgResId.asStateFlow()

    private val _isPaired = MutableStateFlow(evaluateIsPaired())
    val isPaired = _isPaired.asStateFlow()

    // TODO: Nikesh - Make this observable
    val pairedUserEmailList: List<String>
        get() { return currentChildUser.user.parentUserEmails }

    fun onParentEmailAddressChanged(newValue: String) {
        _parentEmailAddress.value = newValue
        _isPaired.value = evaluateIsPaired()
    }

    fun onClickPair() {
        // Check if user has already paired with 3 users
        if (currentChildUser.user.parentUserEmails.size >= 3) {
            _errorMsgResId.value = R.string.pair_screen_max_limit_reached
            return
        }

        viewModelScope.launch {
            // TODO: Handle the scenario where "Unpair" button text is shown
            apiInterface.pairWithParent(currentChildUser.user.id, parentEmailAddress.value)
                .collect { result ->
                    when (result) {
                        is Result.Failure -> {
                            _showProgress.value = false
                            _isPaired.value = evaluateIsPaired()
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
                            _isPaired.value = evaluateIsPaired()
                            _errorMsgResId.value = null

                            // Persist the value
                            val parentUserId = result.data as String
                            currentChildUser.user = currentChildUser.user.copy(
                                parentUserIds = currentChildUser.user.parentUserIds
                                    .apply { add(parentUserId) },
                                parentUserEmails = currentChildUser.user.parentUserEmails
                                    .apply { add(_parentEmailAddress.value) },
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
        // Return true if _parentEmailAddress.value matches with
        // any item in currentChildUser.user.parentUserEmails
        currentChildUser.user.parentUserEmails.forEach { parentUserEmail ->
            if (parentUserEmail == _parentEmailAddress.value) {
                return true
            }
        }
        return false
    }

}