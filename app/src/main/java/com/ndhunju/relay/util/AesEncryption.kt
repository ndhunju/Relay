package com.ndhunju.relay.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesEncryption {

    private const val password = "custom_password"
    private const val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    private const val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

    private val ivParameterSpec by lazy { IvParameterSpec(Base64.decode(iv, Base64.DEFAULT)) }
    private val factory by lazy { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1") }

    private val spec by lazy {
        PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            10000,
            256
        )
    }

    private val secretKey by lazy { factory.generateSecret(spec) }
    private val secretKeySpec by lazy { SecretKeySpec(secretKey.encoded, "AES") }
    private val cipher by lazy { Cipher.getInstance("AES/CBC/PKCS5Padding") }

    fun encrypt(strToEncrypt: String) :  String?
    {
        try
        {
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

    fun decrypt(strToDecrypt : String) : String?
    {
        try
        {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        }
        catch (e : Exception) {
            println("Error while decrypting: $e");
        }
        return null
    }
}