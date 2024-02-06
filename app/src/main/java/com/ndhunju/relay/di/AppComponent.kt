package com.ndhunju.relay.di

import androidx.work.WorkManager
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.UserSettingsPersistService
import com.ndhunju.relay.ui.login.LoginActivity
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

    /**
     * This tells Dagger that [LoginActivity] requests injection so the graph needs to
     * satisfy all the dependencies of the fields that LoginActivity is requesting.
     */
    fun inject(activity: LoginActivity)

}