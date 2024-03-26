package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.UserNotFoundException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PairWithParentViewModel(
    private val apiInterface: ApiInterface,
    private val currentChildUser: CurrentUser,
): ViewModel() {

    private val _selectedParentPhoneAddress = MutableStateFlow(
        currentChildUser.user.getParentPhoneNumbers().firstOrNull() ?: ""
    )
    val selectedParentPhoneAddress = _selectedParentPhoneAddress.asStateFlow()

    private val _showProgress = MutableStateFlow(false)
    val showProgress = _showProgress.asStateFlow()

    private val _errorMsgResId = MutableStateFlow<Int?>(null)
    val errorMsgResId = _errorMsgResId.asStateFlow()

    private val _isSelectedParentPaired = MutableStateFlow(evaluateIsPaired())
    val isSelectedParentPaired = _isSelectedParentPaired.asStateFlow()

    private val _pairedUserPhoneList = MutableStateFlow(currentChildUser.user.getParentPhoneNumbers())
    val pairedUserPhoneList = _pairedUserPhoneList.asStateFlow()

    fun onSelectedParentPhoneChanged(newValue: String) {
        _selectedParentPhoneAddress.value = newValue
        _isSelectedParentPaired.value = evaluateIsPaired()
    }

    fun onClickPairUnpair() {
        if (evaluateIsPaired()) {
            doUnPairWithSelectedParent()
        } else {
            doPairWithSelectedParent()
        }
    }

    private fun doUnPairWithSelectedParent() {
        viewModelScope.launch {
            _showProgress.value = true
            val selectedParent = currentChildUser.user.getParentUsers().first {
                it.phone == selectedParentPhoneAddress.value
            }

            val result = apiInterface.postUnPairWithParent(
                currentChildUser.user.id,
                selectedParent.id
            )

            when (result) {
                is Result.Failure -> {
                    _showProgress.value = false
                    _isSelectedParentPaired.value = evaluateIsPaired()
                    _errorMsgResId.value = R.string.pair_screen_unpair_failed
                }

                is Result.Pending -> _showProgress.value = true
                is Result.Success -> {
                    // Update the local copy
                    currentChildUser.user.removeParent(selectedParent)

                    // Update the UI
                    _showProgress.value = false
                    _errorMsgResId.value = null
                    _isSelectedParentPaired.value = evaluateIsPaired()
                    _pairedUserPhoneList.value = currentChildUser.user.getParentPhoneNumbers()
                }
            }
        }
    }

    private fun doPairWithSelectedParent() {
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
                selectedParentPhoneAddress.value
            )

            when (result) {
                is Result.Failure -> {
                    _showProgress.value = false
                    _isSelectedParentPaired.value = evaluateIsPaired()
                    if (result.throwable is UserNotFoundException) {
                        _errorMsgResId.value = R.string.pair_screen_user_phone_not_found
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
                        phone = _selectedParentPhoneAddress.value
                    ))

                    //userSettingsPersistService.save(currentChildUser.user)

                    // Update the UI
                    _showProgress.value = false
                    _errorMsgResId.value = null
                    _isSelectedParentPaired.value = evaluateIsPaired()
                    _pairedUserPhoneList.value = currentChildUser.user.getParentPhoneNumbers()
                }
            }
        }
    }

    /**
     * Evaluates value of [_isSelectedParentPaired] property
     */
    private fun evaluateIsPaired(): Boolean {
        currentChildUser.user.getParentPhoneNumbers().forEach { parentPhoneNumber ->
            if (parentPhoneNumber == _selectedParentPhoneAddress.value) {
                return true
            }
        }
        return false
    }

    fun onClickPairedUser(phone: String) {
        _selectedParentPhoneAddress.value = phone
        onSelectedParentPhoneChanged(phone)
    }

}