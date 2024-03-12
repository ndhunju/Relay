package com.ndhunju.relay.util

import org.junit.Assert
import org.junit.Test


class AESEncryptionTest {

    @Test
    fun `encrypted string should be correctly decrypted`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = AesEncryption.encrypt(originalMsg)
        val decryptedMsg = AesEncryption.decrypt(encryptedMsg!!)

        Assert.assertEquals(
            "Encrypted text is not decrypted to original text",
            originalMsg,
            decryptedMsg
        )
    }

    @Test
    fun `given a string is encrypted When a different password is used to decrypt Then it should not match`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = AesEncryption.encrypt(originalMsg, "password")
        val decryptedMsg = AesEncryption.decrypt(encryptedMsg!!, "wrong_password")

        Assert.assertNotEquals(
            "Encrypted text is should not be decrypted to original text",
            originalMsg,
            decryptedMsg
        )
    }

    @Test
    fun `given a null password When encrypt is called Then plain text is returned`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = AesEncryption.encrypt(originalMsg, null)

        Assert.assertEquals(
            "Encrypted text should be same as original text is password is null",
            originalMsg,
            encryptedMsg
        )
    }

    @Test
    fun `given an empty password When encrypt is called Then plain text is returned`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = AesEncryption.encrypt(originalMsg, "")

        Assert.assertEquals(
            "Encrypted text should be same as original text is password is empty",
            originalMsg,
            encryptedMsg
        )
    }
}