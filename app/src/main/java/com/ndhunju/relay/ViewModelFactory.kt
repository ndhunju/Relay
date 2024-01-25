package com.ndhunju.relay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.ndhunju.relay.ui.MainViewModel
import com.ndhunju.relay.ui.account.AccountViewModel
import com.ndhunju.relay.ui.pair.PairWithParentViewModel
import com.ndhunju.relay.ui.parent.ChildUserListViewModel
import com.ndhunju.relay.util.CurrentUser

val RelayViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Get the Application object from extras
        val application = checkNotNull(extras[APPLICATION_KEY]) as RelayApplication
        val deviceSmsReaderService = application.appComponent.deviceSmsReaderService()
        val apiInterface = application.appComponent.apiInterface()
        val smsInfoRepository = application.appComponent.smsInfoRepository()
        val userSettingsPersistService = application.appComponent.userSettingsPersistService()
        with(modelClass) {
            return when {
                isAssignableFrom(MainViewModel::class.java) -> {
                    MainViewModel(deviceSmsReaderService, smsInfoRepository, apiInterface) as T
                }
                isAssignableFrom(AccountViewModel::class.java) -> {
                    AccountViewModel(
                        apiInterface,
                        userSettingsPersistService,
                        CurrentUser.user
                    ) as T
                }
                isAssignableFrom(PairWithParentViewModel::class.java) -> {
                    PairWithParentViewModel(
                        apiInterface,
                        CurrentUser,
                        userSettingsPersistService
                    ) as T
                }
                isAssignableFrom(ChildUserListViewModel::class.java) -> {
                    ChildUserListViewModel(apiInterface, CurrentUser.user.id) as T
                }
                else -> throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }
    }
}
