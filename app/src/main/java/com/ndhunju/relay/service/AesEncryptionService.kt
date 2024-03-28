package com.ndhunju.relay.service

import android.util.Base64
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AesEncryptionService(
    private val analyticsProvider: AnalyticsProvider,
): EncryptionService {

    // TODO: Either generate salt at run time along with encryption key
    //  Or get it from the backend to make it safer
    private val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="
    private val iv = "bVQzNFNhRkQ1Njc4UUFaWA=="

    private val ivParameterSpec by lazy { IvParameterSpec(Base64.decode(iv, Base64.DEFAULT)) }
    private val factory by lazy { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1") }
    private val cipher by lazy { Cipher.getInstance("AES/CBC/PKCS5Padding") }

    override fun encrypt(strToEncrypt: String, password: String?): String? {
        // Don't encrypt is the password is null or empty
        if (password.isNullOrEmpty()) {
            return strToEncrypt
        }

        try {
            val spec = PBEKeySpec(
                password.toCharArray(),
                salt.toByteArray(),
                10000,
                256
            )
            val secretKey by lazy { factory.generateSecret(spec) }
            val secretKeySpec by lazy { SecretKeySpec(secretKey.encoded, "AES") }
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            return Base64.encodeToString(
                cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)),
                Base64.DEFAULT
            )
        } catch (e: Exception) {
            analyticsProvider.logEvent("didFailToEncrypt", e.message)
        }

        return null
    }

    override fun decrypt(strToDecrypt: String, password: String?): String? {
        // Don't decrypt if the password is null or empty
        if (password.isNullOrEmpty()) {
            return strToDecrypt
        }

        try {
            val spec = PBEKeySpec(
                password.toCharArray(),
                salt.toByteArray(),
                10000,
                256
            )
            val secretKey by lazy { factory.generateSecret(spec) }
            val secretKeySpec by lazy { SecretKeySpec(secretKey.encoded, "AES") }
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        } catch (e : Exception) {
            analyticsProvider.logEvent("didFailToDecrypt", e.message)
        }

        return null
    }

}