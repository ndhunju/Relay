package com.ndhunju.relay

import com.ndhunju.relay.data.RelayRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
// @Component makes Dagger create a graph of dependencies
@Component(modules = [AndroidAppModule::class])
interface AppComponent {

    // The return type of functions inside the component interface is
    // what can be provided from the container
    fun relayRepository(): RelayRepository

}