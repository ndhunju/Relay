package com.ndhunju.relay.util

import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider

/**
 * Provides API to interact with currently active/logged [User].
 */
interface CurrentUser {
    var user: User
    fun isUserSignedIn(): Boolean
}

/**
 * [CurrentUser] that saves changes in memory only.
 */
class InMemoryCurrentUser: CurrentUser {

    private var localUser = User()

    override var user: User
        get() = localUser
        set(value) { localUser = value}

    override fun isUserSignedIn(): Boolean {
        return user.isRegistered && user.id.isNotEmpty()
    }

}

/**
 * Implements [CurrentUser] such that any changes to [CurrentUser.user] are persisted permanently
 */
class PersistableCurrentUserImpl(
    private val userSettingsPersistService: UserSettingsPersistService,
    private val analyticsProvider: AnalyticsProvider
): CurrentUser {

    private var cachedUser: User? = null

    override var user: User = User()
        get() {
            synchronized(this) {
                if (cachedUser == null) {
                    cachedUser = userSettingsPersistService.retrieve() ?: User()
                    cachedUser?.setOnUserUpdatedListener(this::onUserUpdated)
                }
                return cachedUser as User
            }
        }

        set(value) {
            field = value
            cachedUser = value
            userSettingsPersistService.save(value)
        }

    override fun isUserSignedIn(): Boolean {
        return user.isRegistered && user.id.isNotEmpty()
    }

    private fun onUserUpdated(updatedUser: User) {
        if (updatedUser != user) {
            analyticsProvider.logEvent("didFindInconsistentUserObjectInsideCurrentUser")
        }
        // Update this on the persistent storage too
        userSettingsPersistService.save(updatedUser)
    }

}

data class User(
    val id: String = "",
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val isRegistered: Boolean = false,
    val encryptionKey: String? = null,
    // TODO: Nikesh - Should we merge User and Child class?
    /**
     * Below field is relevant when current user is functioning as a child user.
     * Make it private so that they can't be modified from outside as
     * we need to notify changes via [onUserUpdated] callback.
     **/
    private val parentUsers: MutableList<User> = mutableListOf(),
    /**
     * Below field is relevant when current user is functioning as a parent user
     * Make it private so that they can't be modified from outside as
     * we need to notify changes via [onUserUpdated] callback.
     **/
    private val childUsers: MutableList<User> = mutableListOf()
) {
    @Transient
    private var onUserUpdated: ((User) -> Unit)? = null

    fun setOnUserUpdatedListener(onUserUpdated: ((User) -> Unit)? = null) {
        this.onUserUpdated = onUserUpdated
    }

    fun getChildUsers(): List<User> {
        return childUsers.toList()
    }

    fun getParentUsers(): List<User> {
        return parentUsers.toList()
    }

    fun getParentEmails(): List<String> {
        return parentUsers.mapNotNull { it.email }
    }

    /**
     * Updates related filed with passed [parentUsers]
     */
    fun updateParentUsers(newParentUsers: List<User>) {
        parentUsers.clear()
        parentUsers.addAll(newParentUsers)
        onUserUpdated?.invoke(this)
    }

    fun addParentUser(parentUser: User) {
        parentUsers.add(parentUser)
        onUserUpdated?.invoke(this)
    }

    fun updateChildUsers(newChildUsers: List<User>) {
        childUsers.clear()
        childUsers.addAll(newChildUsers)
        onUserUpdated?.invoke(this)
    }

    private fun findChildIndex(childEmail: String): Int? {
        childUsers.forEachIndexed { i, childUser ->
            if (childUser.email == childEmail) {
                return i
            }
        }

        return null
    }

    /**
     * Adds passed [encryptionKey] for child with email [childUserEmail] is present.
     * Otherwise, returns false
     */
    fun addEncryptionKeyOfChild(childUserEmail: String, encryptionKey: String): Boolean {
        val i = findChildIndex(childUserEmail) ?: return false
        val updatedChildUser = childUsers[i].copy(encryptionKey = encryptionKey)
        childUsers[i] = updatedChildUser
        onUserUpdated?.invoke(this)
        return true
    }

    fun getEncryptionKey(childUserId: String): String? {
        childUsers.forEach { childUser ->
            if (childUser.id == childUserId) {
                return childUser.encryptionKey
            }
        }

        return null
    }
}