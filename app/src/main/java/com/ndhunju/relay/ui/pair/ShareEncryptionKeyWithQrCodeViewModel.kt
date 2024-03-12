package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.ndhunju.relay.util.CurrentUser

class ShareEncryptionKeyWithQrCodeViewModel(
    private val currentUser: CurrentUser,
    private val gson: Gson,
): ViewModel() {

    fun getEncryptionKeyInfo(): String {
        return gson.toJson(
            EncryptionKeyInfo(
                currentUser.user.email,
                currentUser.user.encryptionKey
            )
        )
    }

    fun getBodyText(): String {
        return currentUser.user.email ?: ""
    }

}