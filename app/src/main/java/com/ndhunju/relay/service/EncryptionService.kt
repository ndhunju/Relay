package com.ndhunju.relay.service

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

interface EncryptionService {

    /**
     * Encrypts [strToEncrypt] with given [password]
     */
    fun encrypt(strToEncrypt: String, password: String?): String?

    /**
     * Decrypts [strToDecrypt] with given [password]
     */
    fun decrypt(strToDecrypt: String, password: String?): String?

}

class AesEncryptionService: EncryptionService {

    override fun encrypt(strToEncrypt: String, password: String?): String? {
        return _encrypt(strToEncrypt, password)
    }

    override fun decrypt(strToDecrypt: String, password: String?): String? {
        return _decrypt(strToDecrypt, password)
    }

    private val defaultPassword = "custom_password"
    private val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="
    private val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

    private val ivParameterSpec by lazy { IvParameterSpec(Base64.decode(iv, Base64.DEFAULT)) }
    private val factory by lazy { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1") }
    private val cipher by lazy { Cipher.getInstance("AES/CBC/PKCS5Padding") }

    private fun _encrypt(strToEncrypt: String, password: String? = defaultPassword) :  String?
    {
        // Don't encrypt is the password is null or empty
        if (password.isNullOrEmpty()) {
            return strToEncrypt
        }

        try
        {
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
        }
        catch (e: Exception)
        {
            println("Error while encrypting: $e")
        }
        return null
    }

    private fun _decrypt(strToDecrypt : String, password: String? = defaultPassword) : String?
    {
        // Don't decrypt if the password is null or empty
        if (password.isNullOrEmpty()) {
            return strToDecrypt
        }
        try
        {
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
        }
        catch (e : Exception) {
            println("Error while decrypting: $e");
        }
        return null
    }

}