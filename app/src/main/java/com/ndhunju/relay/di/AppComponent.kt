package com.ndhunju.relay.di

import androidx.work.WorkManager
import com.google.gson.Gson
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.service.AnalyticsManager
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.NotificationManager
import com.ndhunju.relay.service.SimpleKeyValuePersistService
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.ui.BaseActivity
import com.ndhunju.relay.util.CurrentUser
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

    fun workManager(): WorkManager

    fun appStateBroadcastService(): AppStateBroadcastService

    fun simpleKeyValuePersistService(): SimpleKeyValuePersistService

    fun notificationManager(): NotificationManager

    fun analyticsManager(): AnalyticsManager

    fun gson(): Gson

    /**
     * This tells Dagger that [BaseActivity] requests injection so the graph needs to
     * satisfy all the dependencies of the fields that it is requesting.
     */
    fun inject(activity: BaseActivity)

}