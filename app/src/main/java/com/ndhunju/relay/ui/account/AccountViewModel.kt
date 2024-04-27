package com.ndhunju.relay.ui.account

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.NetworkNotFoundException
import com.ndhunju.relay.api.PhoneAlreadyRegisteredException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.api.response.Settings
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.ENC_KEY_MIN_LENGTH
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.extensions.combine
import com.ndhunju.relay.util.isValidEncryptionKey
import com.ndhunju.relay.util.isValidPhoneNumber
import com.ndhunju.relay.util.wrapper.StringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.util.UUID

class AccountAndroidViewModel(
    appStateBroadcastService: AppStateBroadcastService,
    analyticsProvider: AnalyticsProvider,
    apiInterface: ApiInterface,
    currentUser: CurrentUser,
    user: User
): ViewModel() {
   val instance = AccountViewModel(
       viewModelScope,
       appStateBroadcastService,
       analyticsProvider,
       apiInterface,
       currentUser,
       user
   )
}

/**
 * Making this view model not depend on any android specific library so that it could be reused
 * for iOS platform as well in case we use Kotlin Multi Platform structure
 */
class AccountViewModel(
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val appStateBroadcastService: AppStateBroadcastService,
    private var analyticsProvider: AnalyticsProvider,
    private val apiInterface: ApiInterface,
    private var currentUser: CurrentUser,
    private var user: User
) {
    private val _state = MutableStateFlow(AccountScreenUiState())
    val state: StateFlow<AccountScreenUiState>
        get() { return _state.asStateFlow() }

    private val randomEncKey by lazy {
        UUID.randomUUID().toString().subSequence(0, ENC_KEY_MIN_LENGTH.times(2)).toString()
    }

    private val name = MutableStateFlow(user.name)
    private val phone = MutableStateFlow(user.phone)
    // At first account creation, encryption key would be null. In such case, use random key
    private val encKey = MutableStateFlow(user.encryptionKey ?: randomEncKey)
    private val errorStrIdForPhone = MutableStateFlow<Int?>(null)
    private val errorStrResIdForEncKey = MutableStateFlow<StringResource?>(null)
    private val errorStrIdGeneric = MutableStateFlow<Int?>(null)
    private val showProgress = MutableStateFlow(false)

    private val _showAccountUnverifiedDialog = MutableStateFlow(false)
    val showAccountUnverifiedDialog = _showAccountUnverifiedDialog.asStateFlow()
    var onClickAccountUnverifiedDialogBtn: (() -> Unit)? = null

    val onNameChange: (String) -> Unit = {
        name.value = it
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
        errorStrResIdForEncKey.value = if (isValidEncryptionKey(it)) {
            null
        } else {
            StringResource(R.string.account_invalid_enc_key, ENC_KEY_MIN_LENGTH)
        }
    }

    /**
     * Ok button was clicked on the dialog.
     */
    val onClickDialogBtnOk = {
        errorStrIdGeneric.value = null
    }

    /**
     * Delete account was clicked
     */
    val onClickDeleteAccount = {
        // TODO: Make API call to delete the account
    }

    val onClickCreateUpdateUser: () -> Unit = {
        viewModelScope.launch(Dispatchers.IO) {
            if (user.isRegistered) {
                pushUserUpdatesToServer()
            } else {
                val isVerified = verifyThePhoneNumber()
                if (isVerified.not()) {
                    _showAccountUnverifiedDialog.value = true
                    onClickAccountUnverifiedDialogBtn = {
                        _showAccountUnverifiedDialog.value = false
                        viewModelScope.launch { createNewUserInServer() }
                    }
                } else {
                    createNewUserInServer()
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
                name, phone, encKey, errorStrIdForPhone,
                errorStrResIdForEncKey, errorStrIdGeneric, showProgress
            ) {
              name, phone, encKey, errorStrIdForPhone,
              errorStrResForEncKey, errorStrIdGeneric, showProgress ->
                AccountScreenUiState(
                    mode = if (user.isRegistered) Mode.Update else Mode.Create,
                    name = name, phone = phone, encKey = encKey,
                    errorStrIdForPhoneField = errorStrIdForPhone,
                    errorStrResForEncKeyField = errorStrResForEncKey,
                    errorStrIdForGenericError = errorStrIdGeneric,
                    showProgress = showProgress,
                    // Disable phone text field if user is already registered or async in progress
                    isPhoneTextFieldEnabled = showProgress.not() && user.isRegistered.not()
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
     * Verifies if the entered phone number
     */
    private suspend fun verifyThePhoneNumber(): Boolean {
        showProgress.value = true
        val settings = apiInterface.getSettings().getDataOrNull<Settings>()
        showProgress.value = false
        return settings?.byPassAccountCreationNumber == phone.value
    }

    /**
     * Creates new user in the server or cloud database
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun createNewUserInServer() {
        showProgress.value = true
        val result = apiInterface.postUser(
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
                when (result.throwable) {
                    is PhoneAlreadyRegisteredException -> {
                        errorStrIdForPhone.value = R.string.account_phone_registered
                        showProgress.value = false
                    }
                    is NetworkNotFoundException -> {
                        errorStrIdGeneric.value = R.string.default_network_not_found_msg
                        showProgress.value = false
                    }
                    else -> {
                        errorStrIdGeneric.value = R.string.account_user_create_failed
                        showProgress.value = false
                    }
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
            name = name.value
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
        name = state.value.name,
        phone = state.value.phone,
        isRegistered = isRegistered,
        encryptionKey = state.value.encKey
    )
}

/**
 * All possible modes that user could be using [AccountScreen] in.
 */
sealed class Mode {
    data object Create: Mode()
    data object Update: Mode()
}