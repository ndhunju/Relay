package com.ndhunju.relay.di

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.data.OfflineSmsInfoRepository
import com.ndhunju.relay.data.room.MainDatabase
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.ApiInterfaceFireStoreImpl
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.data.OfflineChildSmsInfoRepository
import com.ndhunju.relay.service.AesEncryptionService
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.AppStateBroadcastServiceImpl
import com.ndhunju.relay.service.DataStoreKeyValuePersistService
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.service.EncryptionService
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import com.ndhunju.relay.util.gson.ResultDeserializer
import com.ndhunju.relay.util.gson.ResultSerializer
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.service.UserSettingsPersistServiceSharedPreferenceImpl
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.PersistableCurrentUserImpl
import com.ndhunju.relay.util.connectivity.LollipopNetworkConnectionChecker
import com.ndhunju.relay.util.connectivity.NetworkConnectionChecker
import com.ndhunju.relay.util.connectivity.NougatNetworkConnectionChecker
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
    fun providesGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Result::class.java, ResultSerializer())
        .registerTypeAdapter(Result::class.java, ResultDeserializer())
        .create()

    /**
     * Even though [CurrentUser] is a class owned by us,
     * we can't use @Inject on it since it is an object.
     * Hence, providing its instance from here.
     */
    @Provides
    @Singleton
    fun providesCurrentUser(): CurrentUser = PersistableCurrentUserImpl(provideUserSettingsPersistService())

    @Provides
    @Singleton
    fun providesApiInterface(): ApiInterface {
        return ApiInterfaceFireStoreImpl(
            providesGson(),
            providesCurrentUser(),
            provideEncryptionService(),
            (application as RelayApplication).appComponent.analyticsManager()
        )
    }

    @Provides
    @Singleton
    fun provideSmsInfoRepository(): SmsInfoRepository = OfflineSmsInfoRepository(
        MainDatabase.getDatabase(application).smsInfoDao()
    )

    @Provides
    @Singleton
    fun provideChildSmsInfoRepository(): ChildSmsInfoRepository = OfflineChildSmsInfoRepository(
        MainDatabase.getDatabase(application).childSmsInfoDao()
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

    /**
     * We could have used @Singleton and @Inject annotations in the [AppStateBroadcastServiceImpl]
     * class like in [DeviceSmsReaderService]. But since we want to use the interface,
     * [AppStateBroadcastService], we need to do it the following way.
     */
    @Provides
    @Singleton
    fun provideAppStateBroadcasterService(): AppStateBroadcastService {
        return AppStateBroadcastServiceImpl(
            provideNetworkConnectionChecker(),
            providesCurrentUser()
        )
    }

    // Keeping check for older SDK in case we need to support older SDK in the future
    @SuppressLint("ObsoleteSdkInt")
    @Provides
    @Singleton
    fun provideNetworkConnectionChecker(): NetworkConnectionChecker {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NougatNetworkConnectionChecker(application)
        } else {
            LollipopNetworkConnectionChecker(application)
        }
    }

    @Provides
    @Singleton
    fun provideSimpleKeyValuePersistService(): SimpleKeyValuePersistService {
        return DataStoreKeyValuePersistService(application)
    }

    @Provides
    @Singleton
    fun provideEncryptionService(): EncryptionService {
        return AesEncryptionService()
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

    @Provides
    @Singleton
    fun providesWorkManager(): WorkManager = WorkManager.getInstance(providesContext())

}