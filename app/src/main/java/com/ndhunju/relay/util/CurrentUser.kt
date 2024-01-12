package com.ndhunju.relay.util

/**
 * References currently active/logged user.
 */
object CurrentUser {
    var user: User = User()
}

data class User(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val isRegistered: Boolean = false,
)