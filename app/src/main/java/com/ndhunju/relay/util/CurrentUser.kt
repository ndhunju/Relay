package com.ndhunju.relay.util

import com.ndhunju.relay.service.UserSettingsPersistService

/**
 * Provides API to interact with currently active/logged [User].
 */
interface CurrentUser {

    var user: User
    fun isUserSignedIn(): Boolean
}

/**
 * Implements [CurrentUser] such that any changes to [CurrentUser.user] are persisted permanently
 */
class PersistableCurrentUserImpl(
    private val userSettingsPersistService: UserSettingsPersistService? = null
): CurrentUser {

    override var user: User = userSettingsPersistService?.retrieve() ?: User()
        set(value) {
            field = value
            userSettingsPersistService?.save(value)
        }

    override fun isUserSignedIn(): Boolean {
        return user.isRegistered && user.id.isNotEmpty()
    }

}

data class User(
    val id: String = "",
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val isRegistered: Boolean = false,
    // TODO: Nikesh - Should we merge User and Child class?
    // Below fields are relevant when current user is functioning as a child user
    val parentUserIds: MutableList<String> = mutableListOf(),
    val parentUserEmails: MutableList<String> = mutableListOf(),
    // Below fields are relevant when current user is functioning as a parent user
    val childUserIds: List<String> = listOf(),
    val childUserEmails: List<String> = listOf()
)