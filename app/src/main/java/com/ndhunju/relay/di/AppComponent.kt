package com.ndhunju.relay.di

import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.service.UserSettingsPersistService
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

    fun userSettingsPersistService(): UserSettingsPersistService

}