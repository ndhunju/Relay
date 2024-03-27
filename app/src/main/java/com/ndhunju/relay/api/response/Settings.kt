package com.ndhunju.relay.api.response

data class Settings(
    // NOTE: For Firebase to create and fill values from collection,
    // it needs to have empty constructor
    val byPassAccountCreationNumber: String? = null,
    val minimumVersionCode: Long = 0,
    val androidAppLink: String? = null,
)