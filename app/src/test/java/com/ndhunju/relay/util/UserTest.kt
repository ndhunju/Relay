package com.ndhunju.relay.util

import org.junit.Assert
import org.junit.Before
import org.junit.Test


class UserTest {

    private lateinit var user: User

    @Before
    fun beforeTest() {
        user = User(
            id = "id_parent",
            email = "parent@gmail.com",
            childUsers = mutableListOf(
                User("id_child_1", "email_child_1@gmail.com", encryptionKey = "key_child_1"),
                User("id_child_2", "email_child_2@gmail.com"),
            )
        )
    }

    @Test
    fun `test updateChildUsersWithoutLosingEncryptionKey`() {
        val existingChildUser = user.getChildUsers()[0]
        val existingEncKey = existingChildUser.encryptionKey
        val copyOfExistingChildUserWithOutEncKey = existingChildUser.copy(encryptionKey = null)

        val newChildUsers = mutableListOf(
            copyOfExistingChildUserWithOutEncKey,
            User("id_new_child_2", "new_child_2@gmail.com"),
            User("id_new_child_3", "new_child_3@gmail.com"),
        )

        user.updateChildUsersWithoutLosingEncryptionKey(newChildUsers)
        val updateChildUser = user.findChildUserByEmail(copyOfExistingChildUserWithOutEncKey.email)

        Assert.assertNotNull(
            "Encryption key should not be null",
            updateChildUser?.encryptionKey
        )


        Assert.assertEquals(
            "Encryption Key should match.",
            existingEncKey,
            updateChildUser?.encryptionKey
        )

        val oldChildUserIdThatIsNotInNewList = "id_child_2"

        Assert.assertNull(
            "Old Child User with id $oldChildUserIdThatIsNotInNewList should exits",
            user.findChildUserByEmail(oldChildUserIdThatIsNotInNewList)
        )

        val newChildUserEmailThatIsNotInOldList = "new_child_2@gmail.com"

        Assert.assertNotNull(
            "New Child User with email $newChildUserEmailThatIsNotInOldList should be present",
            user.findChildUserByEmail(newChildUserEmailThatIsNotInOldList)
        )

    }
}