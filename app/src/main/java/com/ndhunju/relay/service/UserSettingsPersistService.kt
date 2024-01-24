package com.ndhunju.relay.service

import android.content.SharedPreferences
import com.google.gson.Gson
import com.ndhunju.relay.util.User
import javax.inject.Inject
import javax.inject.Singleton


interface UserSettingsPersistService {

    /**
     * Saves the pass [user] object persistently
     */
    fun save(user: User)

    /**
     * Retrieves [User] object that was previously save using [save] method
     */
    fun retrieve(): User?

}

@Singleton
class UserSettingsPersistServiceSharedPreferenceImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
): UserSettingsPersistService {

    override fun save(user: User) {
        sharedPreferences.edit().putString(USER_KEY, gson.toJson(user)).apply()
    }

    override fun retrieve(): User? {
        val userInJson = sharedPreferences.getString(USER_KEY, null)
        return gson.fromJson(userInJson, User::class.java)
    }

    companion object {
        const val USER_KEY = "User-Json"
    }
}

/**
 * Dummy implementation of [UserSettingsPersistService].
 * Can be used for UI Preview.
 */
class UserSettingsPersistServiceDummyImpl: UserSettingsPersistService {
    override fun save(user: User) {
        throw Exception("This is a dummy implementation")
    }

    override fun retrieve(): User? {
        throw Exception("This is a dummy implementation")
    }

}