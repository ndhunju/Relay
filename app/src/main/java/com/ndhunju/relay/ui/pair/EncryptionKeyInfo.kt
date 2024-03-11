package com.ndhunju.relay.ui.pair

import com.ndhunju.relay.ui.account.isValidEmail
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class EncryptionKeyInfo(
    val childEmail: String?,
    val encryptionKey: String?,
) {

    @OptIn(ExperimentalContracts::class)
    fun isValid(email: String? = this.childEmail, encryptionKey: String? = this.encryptionKey): Boolean {
        contract {
            returns(true) implies (email != null && encryptionKey != null)
        }
        return email?.isValidEmail() == true
                && encryptionKey.isNullOrEmpty().not()
    }

}