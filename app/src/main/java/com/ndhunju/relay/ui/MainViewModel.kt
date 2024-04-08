package com.ndhunju.relay.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.api.Result
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _showProgress = mutableStateOf(false)
    val showProgress = _showProgress.asState()

    val showUpIcon: State<Boolean> = mutableStateOf(false)

    private val _lastMessageForEachThread  = mutableStateListOf<Message>()
    val lastMessageForEachThread: SnapshotStateList<Message> = _lastMessageForEachThread

    // Note: Compose doesn't track inner fields for changes unless we use mutableStateOf
    private val _showErrorMessageForPermissionDenied = mutableStateOf(false)
    var showErrorMessageForPermissionDenied: State<Boolean> = _showErrorMessageForPermissionDenied

    // var showSearchTextField: Boolean by mutableStateOf(false)
    private val _showSearchTextField = mutableStateOf(false)
    var showSearchTextField: State<Boolean> = _showSearchTextField

    private val _showSplashScreen = MutableStateFlow(true)
    val showSplashScreen = _showSplashScreen.asStateFlow()

    //region UI Events
    val onRefreshByUser: () -> Unit = {
        viewModelScope.launch {
            updateLastMessagesWithCorrectSyncStatus()
            _isRefreshing.value = false
            _showProgress.value = false
        }
    }

    val onClickSearchIcon = {
        _showSearchTextField.value = _showSearchTextField.value.not()
    }

    val onSearchTextChanged: (String) -> Unit = {}
    val onClickAccountIcon = { doOpenAccountFragment?.invoke() }
    val onClickMessage: (Message) -> Unit = { doOpenMessageFromFragment?.invoke(it) }
    val onClickGrantPermission: () -> Unit = { doRequestSmsPermission?.invoke() }

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
    var doRequestSmsPermission: (() -> Unit)? = null

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

    private var latestNewMessageTimeStamp: Long = System.currentTimeMillis()

    private val newMessageObserver = Observer<Long> { newMessageTimeStamp ->
        viewModelScope.launch(Dispatchers.IO) {
            val newMessages = deviceSmsReaderService.getMessagesSince(latestNewMessageTimeStamp)
            latestNewMessageTimeStamp = newMessageTimeStamp
            newMessages.forEach { message ->
                onNewMessageReceived(message)
            }
        }
    }

    private val newSyncedMessageObserver = Observer<List<Message>> { newSyncedMessages ->
        newSyncedMessages.forEach { message ->
            onNewSyncedMessage(message)
        }
    }

    init {
        appStateBroadcastService.newMessagesReceivedTime.observeForever(newMessageObserver)
        appStateBroadcastService.newSyncedMessages.observeForever(newSyncedMessageObserver)
    }

    override fun onCleared() {
        appStateBroadcastService.newMessagesReceivedTime.removeObserver(newMessageObserver)
        appStateBroadcastService.newSyncedMessages.removeObserver(newSyncedMessageObserver)
        super.onCleared()
    }

    val onNewMessageReceived: (Message) -> Unit = { newMessage ->
        // Update the UI to the the latest SMS
        viewModelScope.launch {
            // Update the UI
            val oldLastMessageIndex = findIndexOfMessage(newMessage)
            if (oldLastMessageIndex > -1) {
                // Update last message shown with the new message
                updateMessageAt(oldLastMessageIndex, body = newMessage.body, date = newMessage.date)
                // Show this message at the top
                moveMessageToTopFrom(oldLastMessageIndex)
            } else {
                // Update the entire list since matching thread wasn't found
                updateLastMessagesWithCorrectSyncStatus()
            }
        }
    }

    private fun moveMessageToTopFrom(currentIndex: Int) {
        if (currentIndex != 0) {
            val updatedThread = _lastMessageForEachThread.removeAt(currentIndex)
            _lastMessageForEachThread.add(0, updatedThread)
        }
    }

    val onNewSyncedMessage: (Message) -> Unit = { newSyncedMessage ->
        viewModelScope.launch {
            // Update the UI
            val oldLastMessageIndex = findIndexOfMessage(newSyncedMessage)
            if (oldLastMessageIndex > -1) {
                // Update the icon based on update call status
                updateMessageAt(oldLastMessageIndex, syncStatus = newSyncedMessage.syncStatus)
            } else {
                // Update the entire list since matching thread wasn't found
                updateLastMessagesWithCorrectSyncStatus()
            }
        }
    }

    /**
     * Finds the index of [message] in [_lastMessageForEachThread]
     */
    private fun findIndexOfMessage(message: Message): Int {
        // Find the thread in which the message is sent to
        _lastMessageForEachThread.forEachIndexed { index, lastMessage ->
            if (lastMessage.threadId == message.threadId) {
                return index
            }
        }
        return -1
    }

    /**
     * Updates the [Message] at [index] with passed non null values
     */
    private fun updateMessageAt(
        index: Int,
        body: String? = null,
        date: Long? = null,
        syncStatus: Result<Nothing>? = null
    ) {
        val exitingCopy = _lastMessageForEachThread[index]
        _lastMessageForEachThread[index].copy(
            body = body ?: exitingCopy.body,
            date = date ?: exitingCopy.date,
            syncStatus = syncStatus ?: exitingCopy.syncStatus
        ).let {
            _lastMessageForEachThread[index] = it
        }
    }

    fun setTitle(title: String) {
        _title.value = title
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