package com.ndhunju.relay.ui.messagesfrom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.extensions.asState
import kotlinx.coroutines.launch

class MessagesFromViewModel(
    private val deviceSmsReaderService: DeviceSmsReaderService,
    private val smsInfoRepository: SmsInfoRepository,
): ViewModel() {

    private var _messagesInThread: SnapshotStateList<Message> = mutableStateListOf()
    var messagesInThread: List<Message> = _messagesInThread

    /**
     * NOTE: This is better way than declaring
     * _isLoading = mutableStateOf()
     * isLoading = _isLoading.asState()
     */
    var isLoading by mutableStateOf(true)
        private set

    private var _textMessage = mutableStateOf("")
    var textMessage = _textMessage.asState()

    var onTextMessageChange: ((String) -> Unit) = {
        _textMessage.value = it
    }

    /**
     * Returns list of message for passed [threadId]
     */
    fun getSmsByThreadId(threadId: String) {
        viewModelScope.launch {
            isLoading = true
            val messages = deviceSmsReaderService.getSmsByThreadId(threadId)
            // Populate the syncStatus of each message based on info stored in local database
            smsInfoRepository.getSmsInfoForEachIdInAndroidDb(
                messages.map { message -> message.idInAndroidDb }
            ).forEachIndexed { i, smsInfo ->
                messages[i].syncStatus = smsInfo?.syncStatus
            }

            _messagesInThread.clear()
            // Update the state with the messages.
            _messagesInThread.addAll(messages)
            isLoading = false
        }
    }

    fun sendTextMessage() {
        // TODO: Send text message

    }
}