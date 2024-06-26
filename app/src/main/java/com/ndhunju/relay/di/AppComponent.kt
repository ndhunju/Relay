package com.ndhunju.relay.di

import androidx.work.WorkManager
import com.google.gson.Gson
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.EncryptionService
import com.ndhunju.relay.service.MessagingService
import com.ndhunju.relay.service.NotificationManager
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.ui.BaseActivity
import com.ndhunju.relay.util.CurrentSettings
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.connectivity.NetworkConnectionChecker
import dagger.Component
import javax.inject.Singleton

/**
 * @Component makes Dagger create a graph of dependencies
 */
@Singleton
@Component(modules = [AndroidAppModule::class, AppModule::class])
interface AppComponent {

    // The return type of functions inside the component interface is
    // what can be provided from the container
    fun deviceSmsReaderService(): DeviceSmsReaderService

    fun apiInterface(): ApiInterface

    fun smsInfoRepository(): SmsInfoRepository

    fun childSmsInfoRepository(): ChildSmsInfoRepository

    fun userSettingsPersistService(): UserSettingsPersistService

    fun currentUser(): CurrentUser

    fun currentSettings(): CurrentSettings

    fun workManager(): WorkManager

    fun messagingService(): MessagingService

    fun appStateBroadcastService(): AppStateBroadcastService

    fun simpleKeyValuePersistService(): SimpleKeyValuePersistService

    fun networkConnectionChecker(): NetworkConnectionChecker

    fun encryptionService(): EncryptionService

    fun notificationManager(): NotificationManager

    fun analyticsProvider(): AnalyticsProvider

    fun gson(): Gson

    /**
     * This tells Dagger that [BaseActivity] requests injection so the graph needs to
     * satisfy all the dependencies of the fields that it is requesting.
     */
    fun inject(activity: BaseActivity)

}