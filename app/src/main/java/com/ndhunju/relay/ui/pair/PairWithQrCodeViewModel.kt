package com.ndhunju.relay.ui.pair

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.security.InvalidParameterException

class PairWithQrCodeViewModel(
    private val apiInterface: ApiInterface,
    private val gson: Gson
): ViewModel() {

    private fun getEncryptionInfo(qrCode: String): EncryptionKeyInfo {
        return gson.fromJson(qrCode, EncryptionKeyInfo::class.java)
    }

    fun getJsonString(encryptionKeyInfo: EncryptionKeyInfo): String {
        return gson.toJson(encryptionKeyInfo)
    }

    fun startPairingWithChildUser(barcode: String) = flow {
        emit(Result.Pending())
        try {
            val encryptionKeyInfo = getEncryptionInfo(barcode)
            if (encryptionKeyInfo.isValid()) {
                // See if we can extract the publicIdentifier given the passed barcode is valid
                if (encryptionKeyInfo.publicIdentifier != null
                    && encryptionKeyInfo.encryptionKey != null) {
                    // Make API call on IO to find the child user
                    val childUserResult = withContext(Dispatchers.IO) {
                        apiInterface.postPairWithChild(
                            encryptionKeyInfo.publicIdentifier,
                            encryptionKeyInfo.encryptionKey
                        )
                    }

                    when (childUserResult) {
                        is Result.Failure -> emit(childUserResult)
                        is Result.Pending -> emit(childUserResult)
                        is Result.Success -> emit(childUserResult)
                    }
                }
            } else {
                emit(Result.Failure(InvalidParameterException("QR code is invalid")))
            }
        } catch (ex: Exception) {
            if (ex is JsonSyntaxException) {
                // Thrown when QR code is not what are looking for
                emit(Result.Failure(InvalidParameterException("QR code is invalid")))
            }

            }
    }
}