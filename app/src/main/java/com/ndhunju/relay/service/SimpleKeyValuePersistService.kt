package com.ndhunju.relay.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ndhunju.relay.util.extensions.getOrPut
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

/**
 * Simple interface that provides APIs to store and retrieve value persistently for a given key
 */
interface SimpleKeyValuePersistService {

    /**
     * Saves [value] persistently for passed [key]
     */
    suspend fun save(key: String, value: String)

    /**
     * Saves [value] for passed [key] if no value was saved previously
     */
    suspend fun saveFirstTime(key: String, value: String)

    /**
     * Retrieves value for passed [key] if it was previously stored using [save] function
     */
    suspend fun retrieve(key: String): Flow<String?>

    /**
     * Deletes entry for [key]
     */
    suspend fun delete(key: String)

}

/**
 * Implements [SimpleKeyValuePersistService]
 */
class DataStoreKeyValuePersistService(
    private val context: Context
): SimpleKeyValuePersistService {

    private val mutex = Mutex()
    private val keyCache = mutableMapOf<String, Preferences.Key<String>>()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        "DataStoreKeyValuePersistService"
    )

    override suspend fun save(key: String, value: String) {
        context.dataStore.edit { pref ->
            pref[keyCache.getOrPut(key, stringPreferencesKey(key))] = value
        }
    }

    override suspend fun saveFirstTime(key: String, value: String) {
        mutex.withLock(this) {
            if (retrieve(key).firstOrNull() == null) {
                save(key, value)
            }
        }
    }

    override suspend fun retrieve(key: String): Flow<String?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { pref ->
            pref[keyCache.getOrPut(key, stringPreferencesKey(key))]
        }
    }

    override suspend fun delete(key: String) {
        mutex.withLock(this) {
            context.dataStore.edit { pref ->
                pref.remove(keyCache.getOrPut(key, stringPreferencesKey(key)))
            }
        }
    }

}

object FakeSimpleKeyValuePersistService: SimpleKeyValuePersistService {

    val map = mutableMapOf<String, String>()

    override suspend fun save(key: String, value: String) {
        map[key] = value
    }

    override suspend fun saveFirstTime(key: String, value: String) {
        if (map[key] == null) {
            map[key] = value
        }
    }

    override suspend fun retrieve(key: String): Flow<String?> {
        return flow { map[key] }
    }

    override suspend fun delete(key: String) {
        map.remove(key)
    }

}