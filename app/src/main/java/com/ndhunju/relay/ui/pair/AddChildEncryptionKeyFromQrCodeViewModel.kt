package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.api.UserNotFoundException
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.flow.flow
import java.security.InvalidParameterException

class AddChildEncryptionKeyFromQrCodeViewModel(
    private val currentUser: CurrentUser,
    private val gson: Gson
): ViewModel() {

    private fun tryGettingPairingInfo(qrCode: String): EncryptionKeyInfo {
        return gson.fromJson(qrCode, EncryptionKeyInfo::class.java)
    }

    fun getJsonString(encryptionKeyInfo: EncryptionKeyInfo): String {
        return gson.toJson(encryptionKeyInfo)
    }

    fun addChildEncryptionKey(barcode: String) = flow {
        emit(Result.Pending<Void>())
        try {
            val info = tryGettingPairingInfo(barcode)
            if (info.isValid(info.childPhone, info.encryptionKey)) {
                // If child user has already added current user as parent, add the key.
                // Else show warning
                val isAdded = currentUser.user.addEncryptionKeyOfChild(
                    info.childPhone,
                    info.encryptionKey
                )
                if (isAdded) emit(Result.Success())
                else emit(Result.Failure(UserNotFoundException(info.childPhone)))
            } else {
                emit(Result.Failure(InvalidParameterException("QR code is invalid")))
            }
        } catch (ex: Exception) {
            if (ex is JsonSyntaxException) {
                // Thrown when QR code is not what we are looking for or mal formatted
                emit(Result.Failure(InvalidParameterException("QR code is invalid")))
            }
        }
    }
}