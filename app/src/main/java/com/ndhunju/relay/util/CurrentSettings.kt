package com.ndhunju.relay.util

import com.google.gson.Gson
import com.ndhunju.relay.BuildConfig
import com.ndhunju.relay.api.response.Settings
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

interface CurrentSettings {
    /**
     * This [settings] flow gets notified whenever a new [Settings] is set
     */
    var settings: Flow<Settings>
    fun updateSettings(settings: Settings)
}

class PersistableCurrentSettings(
    private val simpleKeyValuePersistService: SimpleKeyValuePersistService,
    private val coroutineScope: CoroutineScope,
    private val gson: Gson,
): CurrentSettings {

    private val cachedSettings = MutableStateFlow(
        Settings(null, BuildConfig.VERSION_CODE.toLong(), null)
    )

    override var settings: Flow<Settings> = cachedSettings.asStateFlow()

    init {
        coroutineScope.launch {
            retrieveSettingsAndSetToCacheIfNotNull()
        }
    }

    override fun updateSettings(settings: Settings)  {
        // Note: For data classes, == checks each property passed through the constructor
        if (settings == this.cachedSettings.value) {
            return
        }
        cachedSettings.value = settings
        coroutineScope.launch { persistCachedSettings() }
    }

    private suspend fun retrieveSettingsAndSetToCacheIfNotNull() {
        val settingsInJson = simpleKeyValuePersistService.retrieve(KEY_SETTINGS).firstOrNull()
        if (settingsInJson != null) {
            cachedSettings.value = gson.fromJson(settingsInJson, Settings::class.java)
        }
    }

    private suspend fun persistCachedSettings() {
        simpleKeyValuePersistService.save(KEY_SETTINGS, gson.toJson(cachedSettings.value))
    }

    companion object {
        const val KEY_SETTINGS = "KEY_SETTINGS"
    }

}