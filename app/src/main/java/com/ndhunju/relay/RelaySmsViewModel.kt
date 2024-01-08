package com.ndhunju.relay

import android.telephony.SmsMessage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.data.RelayRepository
import com.ndhunju.relay.service.CloudDatabaseService
import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RelaySmsViewModel(
    private val repository: RelayRepository,
    private val cloudDatabaseService: CloudDatabaseService
): ViewModel() {

    private var _state = MutableStateFlow(SmsReporterViewState())
    val state: StateFlow<SmsReporterViewState>
        get() { return _state }

    var onClickSearchIcon = {
        _state.value.showSearchTextField = !_state.value.showSearchTextField
    }

    var onSearchTextChanged: (String) -> Unit = {}
    var onClickAccountIcon = {}
    var onClickMessage: (Message) -> Unit = {}
    var onClickGrantPermission = {}

    var onAllPermissionGranted = {
        // All permissions granted
        state.value.updateMessages(repository.getLastSmsBySender())
        // Reset this value in case it was set to true earlier
        state.value.showErrorMessageForPermissionDenied = false
    }

    var onNewSmsReceived: (SmsMessage) -> Unit = { smsMessage ->
        // TODO: Update Sync icon
        // Push new SMS to the server
        cloudDatabaseService.pushMessage(Message(
            "0",
            smsMessage.originatingAddress ?: "",
            smsMessage.messageBody,
            smsMessage.timestampMillis.toString(),
            ""
        ))

        // Update the UI to the the latest SMS
        viewModelScope.launch {
            var oldLastMessage: Message? = null
            var oldLastMessageIndex = -1

            // Find the thread in which the message is sent to
            state.value.messageList.forEachIndexed { index, message ->
                if (message.from == smsMessage.originatingAddress) {
                    oldLastMessage = state.value.messageList[index]
                    oldLastMessageIndex = index
                }
                return@forEachIndexed
            }

            if (oldLastMessage != null) {
                // Update last message shown with the new message
                oldLastMessage?.copy(
                    body = smsMessage.messageBody,
                    date = smsMessage.timestampMillis.toString()
                )?.let { state.value.messageList[oldLastMessageIndex] = it }
            } else {
                // Update the entire list since matching thread wasn't found
                viewModelScope.launch {
                    state.value.updateMessages(repository.getLastSmsBySender())
                }
            }

        }
    }

    /**
     * Returns list of message for passed [sender]
     */
    fun getSmsByThreadId(sender: String): List<Message> {
        return repository.getSmsByThreadId(sender)
    }

}

data class SmsReporterViewState(
    private var messages: List<Message> = emptyList()
) {
    var messageList = mutableStateListOf<Message>()
    // Note: Compose doesn't track inner fields for change unless we use mutableStateOf
    var showErrorMessageForPermissionDenied: Boolean by mutableStateOf(false)
    var showSearchTextField: Boolean by mutableStateOf(false)

    init {
        updateMessages(messages)
    }

    fun updateMessages(messages: List<Message>) {
        messageList.addAll(messages)
    }
}