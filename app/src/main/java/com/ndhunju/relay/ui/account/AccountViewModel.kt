package com.ndhunju.relay.ui.account

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.EmailAlreadyExistException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.util.regex.Pattern

class AccountViewModel(
    private val appStateBroadcastService: AppStateBroadcastService,
    private var analyticsProvider: AnalyticsProvider,
    private val apiInterface: ApiInterface,
    private var currentUser: CurrentUser,
    private var user: User
): ViewModel() {

    private val _state = MutableStateFlow(AccountScreenUiState())
    val state: StateFlow<AccountScreenUiState>
        get() { return _state.asStateFlow() }

    private val email = MutableStateFlow(user.email)
    private val name = MutableStateFlow(user.name)
    private val phone = MutableStateFlow(user.phone)
    private val encKey = MutableStateFlow(user.encryptionKey)
    private val errorStrIdForEmail = MutableStateFlow<Int?>(null)
    private val errorStrIdForName = MutableStateFlow<Int?>(null)
    private val errorStrIdForPhone = MutableStateFlow<Int?>(null)
    private val errorStrIdGeneric = MutableStateFlow<Int?>(null)
    private val showProgress = MutableStateFlow(false)

    val onEmailChange: (String) -> Unit = {
        email.value = it
        errorStrIdForEmail.value = if (it.isValidEmail()) {
             null
        } else {
            R.string.account_invalid_email
        }
    }

    val onNameChange: (String) -> Unit = {
        name.value = it
        errorStrIdForName.value = if (isValidName(it)) null else R.string.account_invalid_name
    }

    val onPhoneChange: (String) -> Unit = {
        phone.value = it
        errorStrIdForPhone.value = if (isValidPhoneNumber(it)) {
            null
        } else {
            R.string.account_invalid_phone
        }
    }

    val onEncKeyChange: (String) -> Unit = {
        encKey.value = it
    }

    /**
     * Ok button was clicked on the dialog.
     */
    val onClickDialogBtnOk = {
        errorStrIdGeneric.value = null
    }

    val onClickCreateUpdateUser: () -> Unit = {
        viewModelScope.launch(Dispatchers.IO) {
            if (user.isRegistered) pushUserUpdatesToServer() else createNewUserInServer()
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
                encKey,
                errorStrIdForEmail,
                errorStrIdForName,
                errorStrIdForPhone,
                errorStrIdGeneric,
                showProgress
            )
            { email, name, phone, encKey, errorStrIdForEmail,
              errorStrIdForName, errorStrIdForPhone, errorStrIdGeneric, showProgress ->
                AccountScreenUiState(
                    mode = if (user.isRegistered) Mode.Update else Mode.Create,
                    email = email,
                    name = name,
                    phone = phone,
                    encKey = encKey,
                    // Disable email text field if user is already registered or network progress
                    isEmailTextFieldEnabled = user.isRegistered.not() && showProgress.not(),
                    errorStrIdForEmailField = errorStrIdForEmail,
                    errorStrIdForNameField = errorStrIdForName,
                    errorStrIdForPhoneField = errorStrIdForPhone,
                    errorStrIdForGenericError = errorStrIdGeneric,
                    showProgress = showProgress
                )
            }.catch { throwable ->
                analyticsProvider.logEvent(
                    "didCatchErrorCreatingAccountScreenUiState",
                    throwable.message
                )
                errorStrIdGeneric.value = R.string.general_error_message
            }.collect { accountScreenUiState ->
                _state.value = accountScreenUiState
            }
        }
    }

    /**
     * Creates new user in the server or cloud database
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun createNewUserInServer() {
        showProgress.value = true
        val result = apiInterface.postUser(
            name = name.value,
            email = email.value,
            phone = phone.value
        )
        when (result) {
            is Result.Pending -> {}
            is Result.Success -> {
                val userId = result.data
                    ?: throw RuntimeException("User id not provided.")
                // Update local copy of user
                user = createUserFromCurrentState(userId)
                // Update currentUser's user too
                currentUser.user = user
                showProgress.value = false
                onNewUserCreated()
            }
            is Result.Failure -> {
                if (result.throwable is EmailAlreadyExistException) {
                    errorStrIdForEmail.value = R.string.account_duplicate_email
                    showProgress.value = false
                } else {
                    errorStrIdGeneric.value = R.string.account_user_create_failed
                    showProgress.value = false
                }
            }
        }
    }

    /**
     * New [User] was created successfully
     */
    private fun onNewUserCreated() {
        appStateBroadcastService.updateIsUserSignedIn(currentUser.isUserSignedIn())
    }

    /**
     * Pushes updates in [user] to the server or cloud database
     */
    private suspend fun pushUserUpdatesToServer() {
        showProgress.value = true
        val result = apiInterface.putUser(
            name = name.value,
            phone = phone.value
        )

        when (result) {
            is Result.Pending -> showProgress.value = true
            is Result.Success -> {
                // Update local copy of user
                user = createUserFromCurrentState()
                // If this is current user, update it too
                if (user.id == currentUser.user.id) {
                    currentUser.user = user
                }
                showProgress.value = false
            }
            else -> {
                showProgress.value = false
                errorStrIdGeneric.value = R.string.account_user_update_failed
            }
        }
    }

    /**
     * Creates an [User] object based on current [state] with id from [user] object
     */
    private fun createUserFromCurrentState(
        id: String? = null,
        isRegistered: Boolean = true
        ) = User(
        id = id ?: user.id,
        email = state.value.email,
        name = state.value.name,
        phone = state.value.phone,
        isRegistered = isRegistered,
        encryptionKey = state.value.encKey
    )
}

/**
 * Data class representing the state of [AccountScreen]
 */
data class AccountScreenUiState(
    val mode: Mode = Mode.Create,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val encKey: String? = null,
    @StringRes val errorStrIdForEmailField: Int? = null,
    @StringRes val errorStrIdForNameField: Int? = null,
    @StringRes val errorStrIdForPhoneField: Int? = null,
    @StringRes val errorStrIdForGenericError: Int? = null,
    /**
     * True when the app is making network call to create/update [User]
     */
    val showProgress: Boolean = false,
    val showDialog: Boolean = errorStrIdForGenericError != null,
    val isEmailTextFieldEnabled: Boolean = showProgress.not(),
    val isNameTextFieldEnabled: Boolean = showProgress.not(),
    val isPhoneTextFieldEnabled: Boolean = showProgress.not(),
    val isEncKeyTextFieldEnabled: Boolean = showProgress.not(),
) {
    fun isCreateUpdateBtnEnabled(): Boolean {
        return showProgress.not() // Disable when showing progress
                // Disable if there is an error in Email, Name or Phone
                && email?.isValidEmail() == true
                && isValidName(name)
                && isValidPhoneNumber(phone)
    }
}

fun String?.isValidEmail(): Boolean {
    if (this == null) return false
    return PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()
}

private fun isValidName(it: String?) = it != null && it.isEmpty().not()

/**
 * Copied from [Patterns.PHONE].
 * For some reason, [Patterns.PHONE] is null while running the test cases
 */
val phonePattern: Pattern by lazy {
    Pattern.compile( // sdd = space, dot, or dash
        ("(\\+[0-9]+[\\- \\.]*)?" // +<digits><sdd>*
                + "(\\([0-9]+\\)[\\- \\.]*)?" // <digit><digit|sdd>+<digit>
                + "([0-9][0-9\\- \\.]+[0-9])")) // <digit><digit|sdd>+<digit>
}
private fun isValidPhoneNumber(it: String?) = it != null
        && it.length > 8 // Has to be at least 9 digits
        && phonePattern.matcher(it).matches()

/**
 * All possible modes that user could be using [AccountScreen] in.
 */
sealed class Mode {
    data object Create: Mode()
    data object Update: Mode()
}