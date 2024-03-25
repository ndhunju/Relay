package com.ndhunju.relay.ui.account

import androidx.annotation.StringRes
import com.ndhunju.relay.util.isValidEncryptionKey
import com.ndhunju.relay.util.isValidPhoneNumber
import com.ndhunju.relay.util.wrapper.StringResource

/**
 * Data class representing the state of [AccountScreen]
 */
data class AccountScreenUiState(
    val mode: Mode = Mode.Create,
    val name: String? = null,
    val phone: String? = null,
    val encKey: String? = null,
    @StringRes val errorStrIdForPhoneField: Int? = null,
    val errorStrResForEncKeyField: StringResource? = null,
    @StringRes val errorStrIdForGenericError: Int? = null,
    /**
     * True when the app is making network call to create/update [User]
     */
    val showProgress: Boolean = false,
    val showDialog: Boolean = errorStrIdForGenericError != null,
    val isNameTextFieldEnabled: Boolean = showProgress.not(),
    val isPhoneTextFieldEnabled: Boolean = showProgress.not(),
    val isEncKeyTextFieldEnabled: Boolean = showProgress.not(),
) {
    fun isCreateUpdateBtnEnabled(): Boolean {
        return showProgress.not() // Disable when showing progress
                // Disable if there is an error in Phone or Encryption Key
                && isValidPhoneNumber(phone)
                && isValidEncryptionKey(encKey)
    }
}