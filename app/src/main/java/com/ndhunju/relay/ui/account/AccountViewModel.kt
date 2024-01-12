package com.ndhunju.relay.ui.account

import android.util.Log
import android.util.Patterns
import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.R
import com.ndhunju.relay.service.CloudDatabaseService
import com.ndhunju.relay.service.Result
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AccountViewModel(
    private val cloudDatabaseService: CloudDatabaseService,
    private val user: User
): ViewModel() {

    private val _state = MutableStateFlow(AccountScreenUiState())
    val state: StateFlow<AccountScreenUiState>
        get() { return _state }

    private val email = MutableStateFlow(user.email)
    private val name = MutableStateFlow(user.name)
    private val phone = MutableStateFlow(user.phone)
    private val errorStrIdForEmail = MutableStateFlow<Int?>(null)
    private val errorStrIdForName = MutableStateFlow<Int?>(null)
    private val errorStrIdForPhone = MutableStateFlow<Int?>(null)
    private val errorStrIdGeneric = MutableStateFlow<Int?>(null)
    private val showProgress = MutableStateFlow(false)

    val onEmailChange: (String) -> Unit = {
        email.value = it
        errorStrIdForEmail.value = if (PatternsCompat.EMAIL_ADDRESS.matcher(it).matches()) {
             null
        } else {
            R.string.account_invalid_email
        }
    }

    val onNameChange: (String) -> Unit = {
        name.value = it
        errorStrIdForName.value = if (it.isEmpty()) R.string.account_invalid_name else null
    }

    val onPhoneChange: (String) -> Unit = {
        phone.value = it
        errorStrIdForPhone.value = if (Patterns.PHONE.matcher(it).matches()) {
            null
        } else {
            R.string.account_invalid_phone
        }
    }

    /**
     * Ok button was clicked on the dialog.
     */
    val onClickDialogBtnOk = {
        errorStrIdGeneric.value = null
    }

    val onClickCreateUpdateUser: () -> Unit = {
        if (user.isRegistered) {
//            cloudDatabaseService.updateUser(
//                name = name.value,
//                phone = phone.value
//            )
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                cloudDatabaseService.createUser(
                    name = name.value,
                    email = email.value,
                    phone = phone.value
                ).collect { result ->
                    when(result) {
                        Result.Pending -> showProgress.value = true
                        is Result.Success -> showProgress.value = false
                        is Result.Failure -> {
                            errorStrIdGeneric.value = R.string.account_user_create_failed
                            showProgress.value = false
                        }
                    }
                }
            }
        }

    }

    init {
        viewModelScope.launch {
            // Combines the latest value from each of the flows, allowing us to generate a
            // view state instance which only contains the latest values.
            // NOTE: In a complex UI State class representing large compose layout,
            // this approach might be expensive as change in any one field triggers
            // recomposition of the entire layout, top to bottom
            combine(
                email,
                name,
                phone,
                errorStrIdForEmail,
                errorStrIdForName,
                errorStrIdForPhone,
                errorStrIdGeneric,
                showProgress
            )
            { email, name, phone, errorStrIdForEmail,
              errorStrIdForName, errorStrIdForPhone, errorStrIdGeneric, showProgress ->
                AccountScreenUiState(
                    mode = if (user.isRegistered) Mode.Update else Mode.Create,
                    email = email,
                    // Disable email text field if user is already registered or network progress
                    isEmailTextFieldEnabled = user.isRegistered.not() && showProgress.not(),
                    errorStrIdForEmailField = errorStrIdForEmail,
                    name = name,
                    errorStrIdForNameField = errorStrIdForName,
                    phone = phone,
                    errorStrIdForPhoneField = errorStrIdForPhone,
                    errorStrIdForGenericError = errorStrIdGeneric,
                    showProgress = showProgress
                )
            }.catch { throwable ->
                // TODO: Nikesh - User Firebase logger
                Log.d("Error", throwable.localizedMessage, throwable)
                errorStrIdGeneric.value = R.string.general_error_message
            }.collect { accountScreenUiState ->
                _state.value = accountScreenUiState
            }
        }
    }
}

/**
 * Data class representing the state of [AccountScreen]
 */
data class AccountScreenUiState(
    val mode: Mode = Mode.Create,
    val email: String? = null,
    val isEmailTextFieldEnabled: Boolean = true,
    @StringRes val errorStrIdForEmailField: Int? = null,
    val name: String? = null,
    @StringRes val errorStrIdForNameField: Int? = null,
    val phone: String? = null,
    @StringRes val errorStrIdForPhoneField: Int? = null,
    @StringRes val errorStrIdForGenericError: Int? = null,
    /**
     * True when the app is making network call to create/update [User]
     */
    val showProgress: Boolean = false,
    val showDialog: Boolean = errorStrIdForGenericError != null
)

/**
 * All possible modes that user could be using [AccountScreen] in.
 */
sealed class Mode {
    data object Create: Mode()
    data object Update: Mode()
}