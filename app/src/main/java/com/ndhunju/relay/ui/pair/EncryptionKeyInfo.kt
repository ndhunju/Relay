package com.ndhunju.relay.ui.pair

import com.ndhunju.relay.util.isValidPhoneNumber
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class EncryptionKeyInfo(
    val publicIdentifier: String?,
    val encryptionKey: String?,
) {
    @OptIn(ExperimentalContracts::class)
    fun isValid(
        publicIdentifier: String? = this.publicIdentifier,
        encryptionKey: String? = this.encryptionKey
    ): Boolean {
        contract { returns(true) implies (publicIdentifier != null && encryptionKey != null) }
        return isValidPhoneNumber(publicIdentifier) && encryptionKey.isNullOrEmpty().not()
    }
}