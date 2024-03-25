package com.ndhunju.relay.ui.pair

import com.ndhunju.relay.util.isValidPhoneNumber
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class EncryptionKeyInfo(
    val childPhone: String?,
    val encryptionKey: String?,
) {
    @OptIn(ExperimentalContracts::class)
    fun isValid(
        phone: String? = this.childPhone,
        encryptionKey: String? = this.encryptionKey
    ): Boolean {
        contract { returns(true) implies (phone != null && encryptionKey != null) }
        return isValidPhoneNumber(phone) && encryptionKey.isNullOrEmpty().not()
    }
}