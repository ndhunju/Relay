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
}