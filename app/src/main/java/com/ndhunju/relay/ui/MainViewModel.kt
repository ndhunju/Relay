package com.ndhunju.relay.ui

import android.telephony.SmsMessage
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.data.SmsInfo
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.ui.pair.PairWithParentFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val deviceSmsReaderService: DeviceSmsReaderService,
    private val smsInfoRepository: SmsInfoRepository,
    private val apiInterface: ApiInterface
): ViewModel() {

    private var _state = MutableStateFlow(MainScreenUiState())
    private var _messageFromUiState = MutableStateFlow(MessageFromUiState())

    val state: StateFlow<MainScreenUiState>
        get() { return _state }

    /**
     * Represents UI state of [MessagesFromFragment]
     */
    val messageFromUiState: StateFlow<MessageFromUiState>
        get() {return _messageFromUiState}

    // UI Events
    var onClickSearchIcon = {
        _state.value.showSearchTextField = !_state.value.showSearchTextField
    }

    val onSearchTextChanged: (String) -> Unit = {}
    val onClickAccountIcon = { doOpenAccountFragment?.invoke() }
    val onClickMessage: (Message) -> Unit = { doOpenMessageFromFragment?.invoke(it) }
    val onClickGrantPermission = { doRequestPermission?.invoke() }

    val onClickNavItem: (NavItem) -> Unit = { navItem ->
        when (navItem) {
            NavItem.AccountNavItem -> onClickAccountIcon()
            NavItem.PairNavItem -> doOpenPairWithParentFragment?.invoke()
        }
    }

    /**
     * Invoked when [MessagesFromFragment] needs to be opened
     */
    var doOpenMessageFromFragment: ((Message) -> Unit)? = null

    /**
     * Invoked when [PairWithParentFragment] needs to be opened
     */
    var doOpenPairWithParentFragment: (() -> Unit)? = null

    /**
     * Invoked when [AccountFragment] needs to be opened
     */
    var doOpenAccountFragment: (() -> Unit)? = null

    /**
     * Invoked when app needs user permissions
     */
    var doRequestPermission: (() -> Unit)? = null

    val onAllPermissionGranted = {
        // All permissions granted
        viewModelScope.launch { updateLastMessagesWithCorrectSyncStatus() }
        // Reset this value in case it was set to true earlier
        state.value.showErrorMessageForPermissionDenied = false
    }

    val onNewSmsReceived: (SmsMessage) -> Unit = { smsMessage ->

        // Update the UI to the the latest SMS
        viewModelScope.launch {

            // Based on smsMessage, retrieve all info about this message from the database
            val messageFromAndroidDb = deviceSmsReaderService.getMessageByAddressAndBody(
                // We tried to use combination of timestamp and address
                // but turns out smsMessage.timestampMillis is different
                // that time stamp in the sms column for the same sms
                smsMessage.originatingAddress ?: "",
                smsMessage.messageBody
            )

            // Store the message on local database
            val smsInfoToInsert = messageFromAndroidDb.toSmsInfo()
            val idOfInsertedSmsInfo = smsInfoRepository.insertSmsInfo(messageFromAndroidDb.toSmsInfo())

            // Update the UI by thread Id instead of address
            var oldLastMessageIndex = -1
            // Find the thread in which the message is sent to
            state.value.lastMessageList.forEachIndexed { index, lastMessage ->
                if (lastMessage.threadId == messageFromAndroidDb.threadId) {
                    oldLastMessageIndex = index
                    return@forEachIndexed
                }
            }

            if (oldLastMessageIndex > -1) {
                // Update last message shown with the new message
                state.value.lastMessageList[oldLastMessageIndex].copy(
                    body = smsMessage.messageBody,
                    date = smsMessage.timestampMillis.toString()
                ).let { state.value.lastMessageList[oldLastMessageIndex] = it }
            } else {
                // Update the entire list since matching thread wasn't found
                updateLastMessagesWithCorrectSyncStatus()
            }

            // Push new message to the cloud database
            apiInterface.pushMessage(messageFromAndroidDb).collect { result ->
                // Update the sync status in the local DB
                smsInfoRepository.updateSmsInfo(smsInfoToInsert.copy(
                    id = idOfInsertedSmsInfo,
                    syncStatus = result)
                )
                // Update the icon based on update call status
                state.value.lastMessageList[oldLastMessageIndex].copy(
                    syncStatus = result
                ).let { state.value.lastMessageList[oldLastMessageIndex] = it }
            }
        }
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

            // Update the state with the messages
            _messageFromUiState.value.messagesInThread.addAll(messages)
            _messageFromUiState.value.isLoading.value = false
        }
    }

    /**
     * Updates the [MainScreenUiState.lastMessageList] stored in [_state] with
     * correct value for [Message.syncStatus]
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

        state.value.updateLastMessages(lastMessages)
    }

}

/**
 * Data class representing state of [MainScreen]
 * @Note The compiler only uses the properties defined inside the primary constructor for the
 * automatically generated functions. The compiler excludes properties declared inside the
 * class body from the generated implementations.
 */
data class MainScreenUiState(
    private var lastMessageForEachThread: List<Message> = emptyList()
) {
    var lastMessageList = mutableStateListOf<Message>()
    // Note: Compose doesn't track inner fields for change unless we use mutableStateOf
    var showErrorMessageForPermissionDenied: Boolean by mutableStateOf(false)
    var showSearchTextField: Boolean by mutableStateOf(false)

    init {
        updateLastMessages(lastMessageForEachThread)
    }

    fun updateLastMessages(messages: List<Message>) {
        lastMessageList.addAll(messages)
    }
}

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