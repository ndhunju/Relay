package com.ndhunju.relay.service

import com.ndhunju.relay.service.analyticsprovider.LocalAnalyticsProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class AeSEncryptionServiceTest {

    private lateinit var aesEncryptionService: EncryptionService
    private val password = "test_password_123"

    @Before
    fun beforeTest() {
        aesEncryptionService = AesEncryptionService(LocalAnalyticsProvider())
    }

    @Test
    fun `encrypted string should be correctly decrypted`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = aesEncryptionService.encrypt(originalMsg, password)
        val decryptedMsg = aesEncryptionService.decrypt(encryptedMsg!!, password)

        Assert.assertEquals(
            "Encrypted text is not decrypted to original text",
            originalMsg,
            decryptedMsg
        )
    }

    @Test
    fun `given a string is encrypted When a different password is used to decrypt Then it should not match`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = aesEncryptionService.encrypt(originalMsg, "password")
        val decryptedMsg = aesEncryptionService.decrypt(encryptedMsg!!, "wrong_password")

        Assert.assertNotEquals(
            "Encrypted text is should not be decrypted to original text",
            originalMsg,
            decryptedMsg
        )
    }

    @Test
    fun `given a null password When encrypt is called Then plain text is returned`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = aesEncryptionService.encrypt(originalMsg, null)

        Assert.assertEquals(
            "Encrypted text should be same as original text is password is null",
            originalMsg,
            encryptedMsg
        )
    }

    @Test
    fun `given an empty password When encrypt is called Then plain text is returned`() {
        val originalMsg = "This is a test message"
        val encryptedMsg = aesEncryptionService.encrypt(originalMsg, "")

        Assert.assertEquals(
            "Encrypted text should be same as original text is password is empty",
            originalMsg,
            encryptedMsg
        )
    }
}