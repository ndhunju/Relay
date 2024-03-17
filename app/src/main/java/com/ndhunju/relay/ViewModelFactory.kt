package com.ndhunju.relay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.ndhunju.relay.ui.MainViewModel
import com.ndhunju.relay.ui.account.AccountViewModel
import com.ndhunju.relay.ui.debug.DebugViewModel
import com.ndhunju.relay.ui.pair.AddChildEncryptionKeyFromQrCodeViewModel
import com.ndhunju.relay.ui.pair.PairWithParentViewModel
import com.ndhunju.relay.ui.pair.PairWithQrCodeViewModel
import com.ndhunju.relay.ui.pair.ShareEncryptionKeyWithQrCodeViewModel
import com.ndhunju.relay.ui.parent.ChildUserListViewModel
import com.ndhunju.relay.ui.parent.messagesfromchild.MessagesFromChildViewModel
import com.ndhunju.relay.ui.parent.messagesinthreadfromchild.MessagesInThreadFromChildVM

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
        val currentUser = appComponent.currentUser()
        val workManager = appComponent.workManager()
        val analyticsManager = appComponent.analyticsProvider()
        val appStateBroadcasterService = appComponent.appStateBroadcastService()
        val gson = appComponent.gson()
        with(modelClass) {
            return when {
                isAssignableFrom(MainViewModel::class.java) -> {
                    MainViewModel(
                        deviceSmsReaderService,
                        smsInfoRepository,
                        appStateBroadcasterService
                    ) as T
                }
                isAssignableFrom(AccountViewModel::class.java) -> {
                    AccountViewModel(
                        appStateBroadcasterService,
                        analyticsManager,
                        apiInterface,
                        currentUser,
                        currentUser.user
                    ) as T
                }
                isAssignableFrom(PairWithParentViewModel::class.java) -> {
                    PairWithParentViewModel(
                        apiInterface,
                        currentUser,
                    ) as T
                }
                isAssignableFrom(ChildUserListViewModel::class.java) -> {
                    ChildUserListViewModel(
                        apiInterface,
                        workManager,
                        currentUser,
                    ) as T
                }
                isAssignableFrom(MessagesFromChildViewModel::class.java) -> {
                    MessagesFromChildViewModel(apiInterface, childSmsInfoRepository) as T
                }
                isAssignableFrom(MessagesInThreadFromChildVM::class.java) -> {
                    MessagesInThreadFromChildVM(childSmsInfoRepository) as T
                }
                isAssignableFrom(DebugViewModel::class.java) -> {
                    DebugViewModel() as T
                }
                isAssignableFrom(PairWithQrCodeViewModel::class.java) -> {
                    PairWithQrCodeViewModel(apiInterface, gson) as T
                }
                isAssignableFrom(AddChildEncryptionKeyFromQrCodeViewModel::class.java) -> {
                    AddChildEncryptionKeyFromQrCodeViewModel(currentUser, gson) as T
                }
                isAssignableFrom(ShareEncryptionKeyWithQrCodeViewModel::class.java) -> {
                    ShareEncryptionKeyWithQrCodeViewModel(currentUser, gson) as T
                }
                else -> throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }
    }
}
