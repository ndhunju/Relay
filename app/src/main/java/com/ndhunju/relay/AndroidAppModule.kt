package com.ndhunju.relay

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

// @Module informs Dagger that this class is a Dagger Module
//  You can use the @Provides annotation in Dagger modules to tell
//  Dagger how to provide classes that your project doesn't own
@Module
class AndroidAppModule(private val application: Application) {

    // @Provides tell Dagger how to create instances of the type that this function returns
    // Function parameters are the dependencies of this type.
    @Provides
    @Singleton
    fun providesApplication(): Application = application

    @Provides
    @Singleton
    fun providesContext(): Context = application

}