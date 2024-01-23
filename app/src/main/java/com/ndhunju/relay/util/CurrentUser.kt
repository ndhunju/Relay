package com.ndhunju.relay.util

/**
 * References currently active/logged user.
 */
object CurrentUser {
    var user: User = User()
    fun isUserSignedIn(): Boolean {
        return user.isRegistered && user.id.isNotEmpty()
    }
}

data class User(
    val id: String = "",
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val isRegistered: Boolean = false,
)