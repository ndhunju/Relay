package com.ndhunju.relay.ui.account

import android.util.Patterns
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.service.CloudDatabaseService
import com.ndhunju.relay.util.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val errorMsg = MutableStateFlow("")

    val onEmailChange: (String) -> Unit = {
        email.value = it
        if (PatternsCompat.EMAIL_ADDRESS.matcher(it).matches().not()) {
            errorMsg.value = "Invalid Email."
        }
    }

    val onNameChange: (String) -> Unit = {
        name.value = it
        if (it.isEmpty()) {
            errorMsg.value = "Name is empty."
        }
    }

    val onPhoneChange: (String) -> Unit = {
        phone.value = it
        if (Patterns.PHONE.matcher(it).matches().not()) {
            errorMsg.value = "Invalid phone number."
        }
    }

    val onClickCreateUpdateUser: () -> Unit = {
        if (user.isRegistered) {
            cloudDatabaseService.createUser(
                name = name.value,
                email = email.value,
                phone = phone.value
            )
        } else {
//            cloudDatabaseService.updateUser(
//                name = name.value,
//                phone = phone.value
//            )
        }

    }

    init {
        viewModelScope.launch {
            // Combines the latest value from each of the flows, allowing us to generate a
            // view state instance which only contains the latest values.
            combine(email, name, phone, errorMsg) { email, name, phone, errorMsg ->
                AccountScreenUiState(
                    mode = if (user.isRegistered) Mode.Update else Mode.Create,
                    email = email,
                    isEmailTextFieldEnabled = user.isRegistered.not(),
                    name = name,
                    phone = phone,
                    errorMsgForNameField = errorMsg
                )
            }.catch { throwable ->
                errorMsg.value = throwable.localizedMessage ?: ""
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
    val errorMsgForEmailField: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val errorMsgForPhoneField: String? = null,
    val errorMsgForNameField: String? = null
)

/**
 * All possible modes that user could be using [AccountScreen] in.
 */
sealed class Mode {
    data object Create: Mode()
    data object Update: Mode()
}