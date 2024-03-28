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
            phone = "+14083207200",
            childUsers = mutableListOf(
                User("id_child_1", "+123456789", encryptionKey = "key_child_1"),
                User("id_child_2", "+987654321"),
            )
        )
    }

    @Test
    fun `test updateChildUsersWithoutLosingEncryptionKey`() {
        val existingChildUser = user.getChildUsers()[0]
        val existingEncKey = existingChildUser.encryptionKey
        val copyOfExistingChildUserWithOutEncKey = existingChildUser.copy(encryptionKey = null)

        val oldChildUserIdThatIsNotInNewList = "id_child_2"
        val newChildUserPhoneThatIsNotInOldList = "+1234567756"

        val newChildUsers = mutableListOf(
            copyOfExistingChildUserWithOutEncKey,
            User(oldChildUserIdThatIsNotInNewList, newChildUserPhoneThatIsNotInOldList),
            User("id_new_child_3", "+123456789"),
        )

        user.updateChildUsersWithoutLosingEncryptionKey(newChildUsers)
        val updateChildUser = user.findChildUserByPhoneNumber(
            copyOfExistingChildUserWithOutEncKey.phone
        )

        Assert.assertNotNull(
            "Encryption key should not be null",
            updateChildUser?.encryptionKey
        )

        Assert.assertEquals(
            "Encryption Key should match.",
            existingEncKey,
            updateChildUser?.encryptionKey
        )

        Assert.assertNotNull(
            "Old Child User with id $oldChildUserIdThatIsNotInNewList should exits",
            user.findChildUserById(oldChildUserIdThatIsNotInNewList)
        )

        Assert.assertNotNull(
            "New Child User with phone $newChildUserPhoneThatIsNotInOldList should be present",
            user.findChildUserByPhoneNumber(newChildUserPhoneThatIsNotInOldList)
        )

    }
}