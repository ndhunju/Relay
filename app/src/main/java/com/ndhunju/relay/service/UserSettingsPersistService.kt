package com.ndhunju.relay.service

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.gson.Gson
import com.ndhunju.relay.util.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsPersistService @Inject constructor(
    application: Application,
    private val gson: Gson
) {

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "encrypted_preferences",
        "masterKeyAlias",
        application,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(user: User) {
        sharedPreferences.edit().putString(USER_KEY, gson.toJson(user)).apply()
    }

    fun retrieve(): User? {
        val userInJson = sharedPreferences.getString(USER_KEY, null)
        return gson.fromJson(userInJson, User::class.java)
    }

    companion object {
        const val USER_KEY = "User-Json"
    }
}