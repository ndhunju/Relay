package com.ndhunju.relay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.ndhunju.relay.ui.MainViewModel
import com.ndhunju.relay.ui.account.AccountViewModel
import com.ndhunju.relay.ui.pair.PairWithParentViewModel
import com.ndhunju.relay.ui.parent.ChildUserListViewModel
import com.ndhunju.relay.ui.parent.messagesfromchild.MessagesFromChildViewModel
import com.ndhunju.relay.ui.parent.messagesinthreadfromchild.MessagesInThreadFromChildVM
import com.ndhunju.relay.util.CurrentUser

val RelayViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Get the Application object from extras
        val appComponent = (checkNotNull(extras[APPLICATION_KEY]) as RelayApplication).appComponent
        val deviceSmsReaderService = appComponent.deviceSmsReaderService()
        val apiInterface = appComponent.apiInterface()
        val smsInfoRepository = appComponent.smsInfoRepository()
        val childSmsInfoRepository = appComponent.childSmsInfoRepository()
        val userSettingsPersistService = appComponent.userSettingsPersistService()
        val workManager = appComponent.workManager()
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
                    ChildUserListViewModel(
                        apiInterface,
                        workManager,
                        CurrentUser,
                        userSettingsPersistService
                    ) as T
                }
                isAssignableFrom(MessagesFromChildViewModel::class.java) -> {
                    MessagesFromChildViewModel(apiInterface, childSmsInfoRepository) as T
                }
                isAssignableFrom(MessagesInThreadFromChildVM::class.java) -> {
                    MessagesInThreadFromChildVM(childSmsInfoRepository) as T
                }
                else -> throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }
    }
}
