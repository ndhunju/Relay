package com.ndhunju.relay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.ndhunju.relay.ui.account.AccountViewModel
import com.ndhunju.relay.util.CurrentUser

val RelayViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Get the Application object from extras
        val application = checkNotNull(extras[APPLICATION_KEY]) as RelayApplication
        val repository = application.appComponent.deviceSmsReaderService()
        val cloudDatabaseService = application.appComponent.cloudDatabaseService()
        val smsRepository = application.appComponent.smsInfoRepository()
        val userSettingsPersistService = application.appComponent.userSettingsPersistService()
        with(modelClass) {
            return when {
                isAssignableFrom(RelaySmsViewModel::class.java) -> {
                    RelaySmsViewModel(repository, smsRepository, cloudDatabaseService) as T
                }
                isAssignableFrom(AccountViewModel::class.java) -> {
                    AccountViewModel(
                        cloudDatabaseService,
                        userSettingsPersistService,
                        CurrentUser.user
                    ) as T
                }
                else -> throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }
    }
}
