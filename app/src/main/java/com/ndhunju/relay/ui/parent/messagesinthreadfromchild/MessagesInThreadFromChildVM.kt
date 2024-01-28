package com.ndhunju.relay.ui.parent.messagesinthreadfromchild

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.ui.MessageFromUiState
import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesInThreadFromChildVM(
    private val childSmsInfoRepository: ChildSmsInfoRepository
): ViewModel() {

    private val _messageFromUiState = MutableStateFlow(MessageFromUiState())
    val messageFromUiState = _messageFromUiState.asStateFlow()

    /**
     * Address (eg. 408 320 7231) of the sender of the message
     */
    var senderAddress: String? = null

    /**
     * Loads [Message] for passed [childUserId] and [threadId]
     */
    fun loadMessagesForChildAndThread(childUserId: String, threadId: String) {
        viewModelScope.launch {
            _messageFromUiState.value.isLoading.value = true
            childSmsInfoRepository.getAllChildSmsInfoOfChildAndThread(childUserId, threadId)
                .collect { childSmsInfoList ->
                    _messageFromUiState.value.messagesInThread.addAll(childSmsInfoList.map {
                        Message(
                            it.idInAndroidDb,
                            it.threadId,
                            it.from,
                            it.body,
                            it.date,
                            it.type,
                            null,
                            it.extra
                        )
                    })

                    _messageFromUiState.value.isLoading.value = false
                }
        }
    }
}