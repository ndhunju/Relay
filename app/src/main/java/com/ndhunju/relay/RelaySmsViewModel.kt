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

    private var _state = MutableStateFlow(RelaySmsAppUiState())
    val state: StateFlow<RelaySmsAppUiState>
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

        // Update the UI to the the latest SMS
        viewModelScope.launch {
            var oldLastMessageIndex = -1

            // Find the thread in which the message is sent to
            state.value.messageList.forEachIndexed { index, message ->
                if (message.from == smsMessage.originatingAddress) {
                    oldLastMessageIndex = index
                }
                return@forEachIndexed
            }

            if (oldLastMessageIndex > -1) {
                // Update last message shown with the new message
                state.value.messageList[oldLastMessageIndex].copy(
                    body = smsMessage.messageBody,
                    date = smsMessage.timestampMillis.toString()
                ).let { state.value.messageList[oldLastMessageIndex] = it }
            } else {
                // Update the entire list since matching thread wasn't found
                state.value.updateMessages(repository.getLastSmsBySender())
            }

            // Push new SMS to the server
            cloudDatabaseService.pushMessage(
                Message(
                    "0",
                    smsMessage.originatingAddress ?: "",
                    smsMessage.messageBody,
                    smsMessage.timestampMillis.toString(),
                    ""
                )
            ).collect { result ->
                // Update the icon based on update call status
                // TODO: Nikesh - Need to sync the state with MessageFrom screen too
                state.value.messageList[oldLastMessageIndex].copy(
                    syncStatus = result
                ).let { state.value.messageList[oldLastMessageIndex] = it }
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

/**
 * Data class representing state of [RelaySmsAppScreen]
 * @Note The compiler only uses the properties defined inside the primary constructor for the
 * automatically generated functions. The compiler excludes properties declared inside the
 * class body from the generated implementations.
 */
data class RelaySmsAppUiState(
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