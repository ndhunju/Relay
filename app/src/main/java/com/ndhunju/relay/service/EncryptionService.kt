package com.ndhunju.relay.service

import com.ndhunju.relay.util.AesEncryption

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
        return AesEncryption.encrypt(strToEncrypt, password)
    }

    override fun decrypt(strToDecrypt: String, password: String?): String? {
        return AesEncryption.decrypt(strToDecrypt, password)
    }

}