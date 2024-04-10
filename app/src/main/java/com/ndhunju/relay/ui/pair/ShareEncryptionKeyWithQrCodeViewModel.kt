package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.ndhunju.relay.R
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.wrapper.StringResource

class ShareEncryptionKeyWithQrCodeViewModel(
    private val currentUser: CurrentUser,
    private val gson: Gson,
): ViewModel() {

    fun getEncryptionKeyInfo(): String {
        return gson.toJson(
            EncryptionKeyInfo(
                currentUser.user.phone,
                currentUser.user.encryptionKey
            )
        )
    }

    fun getBodyText(): StringResource? {
        val phone = currentUser.user.phone ?: return null
        return StringResource(R.string.share_enc_key_screen_body, phone)
    }

}