package com.ndhunju.relay.di

import android.app.Application
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.data.OfflineSmsInfoRepository
import com.ndhunju.relay.data.MainDatabase
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.service.ApiInterface
import com.ndhunju.relay.service.ApiInterfaceFireStoreImpl
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.service.UserSettingsPersistServiceSharedPreferenceImpl
import com.ndhunju.relay.util.CurrentUser
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Dagger module that provides object not owned by us.
 * @Note: @Module informs Dagger that this class is a Dagger Module
 * You can use the @Provides annotation in Dagger modules to tell
 * Dagger how to provide classes that your project doesn't own.
 */
@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesGson(): Gson = GsonBuilder().create()

    /**
     * Even though [CurrentUser] is a class owned by us,
     * we can't use @Inject on it since it is an object.
     * Hence, providing its instance from here.
     */
    @Provides
    @Singleton
    fun providesCurrentUser(): CurrentUser = CurrentUser

    @Provides
    @Singleton
    fun providesApiInterface(): ApiInterface {
        return ApiInterfaceFireStoreImpl(providesGson(), providesCurrentUser())
    }

    @Provides
    @Singleton
    fun provideSmsInfoRepository(): SmsInfoRepository = OfflineSmsInfoRepository(
        MainDatabase.getDatabase(application).smsInfoDao()
    )

    @Provides
    @Singleton
    fun provideUserSettingsPersistService(): UserSettingsPersistService {
        val application = application as RelayApplication
        return UserSettingsPersistServiceSharedPreferenceImpl(
            EncryptedSharedPreferences.create(
                "encrypted_preferences",
                "masterKeyAlias",
                application,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ),
            providesGson()
        )
    }
}

/**
 * Dagger module that provides Android specific objects.
 */
@Module
class AndroidAppModule(private val application: Application) {

    /**
     * @Note @Provides tell Dagger how to create instances of
     * the type that this function returns. Function parameters
     * are the dependencies of this type.
     */
    @Provides
    @Singleton
    fun providesApplication(): Application = application

    @Provides
    @Singleton
    fun providesContext(): Context = application

}