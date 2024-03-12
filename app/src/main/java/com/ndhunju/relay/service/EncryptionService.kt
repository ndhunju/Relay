package com.ndhunju.relay.service

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

