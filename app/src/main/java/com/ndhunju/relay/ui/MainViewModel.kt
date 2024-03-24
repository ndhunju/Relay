package com.ndhunju.relay.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.data.SmsInfo
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.debug.DebugFragment
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.ui.pair.PairWithChildByScanningQrCodeActivity
import com.ndhunju.relay.ui.pair.PairWithParentFragment
import com.ndhunju.relay.ui.parent.ChildUserListFragment
import com.ndhunju.relay.util.extensions.asState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val deviceSmsReaderService: DeviceSmsReaderService,
    private val smsInfoRepository: SmsInfoRepository,
    private val appStateBroadcastService: AppStateBroadcastService
): ViewModel() {

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _isRefreshing = mutableStateOf(false)
    val isRefresh = _isRefreshing.asState()

    val showUpIcon: State<Boolean> = mutableStateOf(false)

    private val _lastMessageForEachThread  = mutableStateListOf<Message>()
    val lastMessageForEachThread: SnapshotStateList<Message> = _lastMessageForEachThread

    // Note: Compose doesn't track inner fields for changes unless we use mutableStateOf
    private val _showErrorMessageForPermissionDenied = mutableStateOf(false)
    var showErrorMessageForPermissionDenied: State<Boolean> = _showErrorMessageForPermissionDenied

    // var showSearchTextField: Boolean by mutableStateOf(false)
    private val _showSearchTextField = mutableStateOf(false)
    var showSearchTextField: State<Boolean> = _showSearchTextField


    // TODO: Nikesh - Remove this as well
    private var _messageFromUiState = MutableStateFlow(MessageFromUiState())

    private val _showSplashScreen = MutableStateFlow(true)
    val showSplashScreen = _showSplashScreen.asStateFlow()

    /**
     * Represents UI state of [MessagesFromFragment]
     */
    val messageFromUiState: StateFlow<MessageFromUiState>
        get() {return _messageFromUiState}

    //region UI Events
    val onRefreshByUser = {
       // TODO: Refresh the data in the UI
    }

    val onClickSearchIcon = {
        _showSearchTextField.value = _showSearchTextField.value.not()
    }

    val onSearchTextChanged: (String) -> Unit = {}
    val onClickAccountIcon = { doOpenAccountFragment?.invoke() }
    val onClickMessage: (Message) -> Unit = { doOpenMessageFromFragment?.invoke(it) }
    val onClickGrantPermission: () -> Unit = { doRequestPermission?.invoke() }

    val onClickNavItem: (NavItem) -> Unit = { navItem ->
        when (navItem) {
            NavItem.AccountNavItem -> onClickAccountIcon()
            NavItem.PairWithParentNavItem -> doOpenPairWithParentFragment?.invoke()
            NavItem.PairWithChildNavItem -> doOpenPairWithChild?.invoke()
            NavItem.ChildUsersNavItem -> doOpenChildUserFragment?.invoke()
            NavItem.EncryptionKeyNavItem -> doOpenEncryptionKeyScreen?.invoke()
        }
    }

    //endregion

    //region Action Callbacks

    /**
     * Invoked when [MessagesFromFragment] needs to be opened
     */
    var doOpenMessageFromFragment: ((Message) -> Unit)? = null

    /**
     * Invoked when [PairWithParentFragment] needs to be opened
     */
    var doOpenPairWithParentFragment: (() -> Unit)? = null

    /**
     * Invoked when [PairWithChildByScanningQrCodeActivity] needs to be opened
     */
    var doOpenPairWithChild: (() -> Unit)? = null

    /**
     * Invoked when screen that shows encryption key needs to be opened
     */
    var doOpenEncryptionKeyScreen: (() -> Unit)? = null

    /**
     * Invoked when [AccountFragment] needs to be opened
     */
    var doOpenAccountFragment: (() -> Unit)? = null

    /**
     * Invoked when [ChildUserListFragment] needs to be opened
     */
    var doOpenChildUserFragment: (() -> Unit)? = null

    /**
     * Invoked when [DebugFragment] needs to be opened
     */
    var doOpenDebugFragment: (() -> Unit)? = null

    /**
     * Invoked when app needs user permissions
     */
    var doRequestPermission: (() -> Unit)? = null

    //endregion

    val onSmsPermissionGranted = {
        // All permissions granted
        viewModelScope.launch { updateLastMessagesWithCorrectSyncStatus() }
        // Reset this value in case it was set to true earlier
        _showErrorMessageForPermissionDenied.value = false
    }

    val onSmsPermissionDenied = {
        updateLastMessages(null)
        _showErrorMessageForPermissionDenied.value = true
        // Hide Splash Screen so that the error message can be shown
        _showSplashScreen.value = false
    }

    private val newMessageObserver = Observer<List<Message>> { newMessages ->
        newMessages.forEach { message ->
            onNewSmsReceived(message)
        }
    }

    init {
        appStateBroadcastService.newProcessedMessages.observeForever(newMessageObserver)
    }

    override fun onCleared() {
        appStateBroadcastService.newProcessedMessages.removeObserver(newMessageObserver)
        super.onCleared()
    }

    val onNewSmsReceived: (Message) -> Unit = { messageFromAndroidDb ->

        // Update the UI to the the latest SMS
        viewModelScope.launch {

            // Update the UI by thread Id instead of address
            var oldLastMessageIndex = -1
            // Find the thread in which the message is sent to
            _lastMessageForEachThread.forEachIndexed { index, lastMessage ->
                if (lastMessage.threadId == messageFromAndroidDb.threadId) {
                    oldLastMessageIndex = index
                    return@forEachIndexed
                }
            }

            if (oldLastMessageIndex > -1) {
                // Update last message shown with the new message
                _lastMessageForEachThread[oldLastMessageIndex].copy(
                    body = messageFromAndroidDb.body,
                    date = messageFromAndroidDb.date
                ).let { _lastMessageForEachThread[oldLastMessageIndex] = it }

                // Update the icon based on update call status
                _lastMessageForEachThread[oldLastMessageIndex].copy(
                    syncStatus = messageFromAndroidDb.syncStatus
                ).let { _lastMessageForEachThread[oldLastMessageIndex] = it }
            } else {
                // Update the entire list since matching thread wasn't found
                updateLastMessagesWithCorrectSyncStatus()
            }
        }
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    /**
     * Returns list of message for passed [threadId]
     */
    fun getSmsByThreadId(threadId: String) {
        viewModelScope.launch {
            _messageFromUiState.value.isLoading.value = true
            val messages = deviceSmsReaderService.getSmsByThreadId(threadId)
            // Populate the syncStatus of each message based on info stored in local database
            smsInfoRepository.getSmsInfoForEachIdInAndroidDb(
                messages.map { message -> message.idInAndroidDb }
            ).forEachIndexed { i, smsInfo ->
                messages[i].syncStatus = smsInfo?.syncStatus
            }

            // Since we are using same instance of this model, clear the messages
            // in case it stored messages from another thread in previous use
            _messageFromUiState.value.messagesInThread.clear()
            // Update the state with the messages.
            _messageFromUiState.value.messagesInThread.addAll(messages)
            _messageFromUiState.value.isLoading.value = false
        }
    }

    /**
     * Updates the [lastMessageForEachThread] with correct value for [Message.syncStatus]
     */
    private suspend fun updateLastMessagesWithCorrectSyncStatus() {
        // Update syncStatus info of Last Message with info available in database
        val lastMessages = deviceSmsReaderService.getLastMessageForEachThread()
        val smsInfoForLastMessages = smsInfoRepository.getSmsInfoForEachIdInAndroidDb(
            lastMessages.map { msg -> msg.idInAndroidDb }
        )

        lastMessages.forEachIndexed{ index, message ->
            message.syncStatus = smsInfoForLastMessages[index]?.syncStatus
        }

        updateLastMessages(lastMessages)
        // Hide splash screen
        _showSplashScreen.value = false
    }

    private fun updateLastMessages(messages: List<Message>?) {
        _lastMessageForEachThread.clear()
        _lastMessageForEachThread.addAll(messages ?: emptyList())
    }

}

/**
 * Data class representing state of [MainScreen]
 * @Note The compiler only uses the properties defined inside the primary constructor for the
 * automatically generated functions. The compiler excludes properties declared inside the
 * class body from the generated implementations.
 */
data class MessageFromUiState(
    var messagesInThread: SnapshotStateList<Message> = mutableStateListOf(),
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
)

/**
 * Convenience function to convert [Message] object to [SmsInfo]
 */
fun Message.toSmsInfo(): SmsInfo {
    return SmsInfo(
        0, // The database framework ignores the provided value because of autogenerate
        idInAndroidDb,
        threadId,
        date,
        syncStatus
    )
}