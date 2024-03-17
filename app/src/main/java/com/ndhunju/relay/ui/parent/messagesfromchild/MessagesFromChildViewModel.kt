package com.ndhunju.relay.ui.parent.messagesfromchild

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.ui.MainScreenUiState
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessagesFromChildViewModel(
    private val apiInterface: ApiInterface,
    private val childSmsInfoRepository: ChildSmsInfoRepository
): ViewModel() {

    private var _state = MutableStateFlow(MainScreenUiState(showUpIcon = mutableStateOf(true)))
    val state: StateFlow<MainScreenUiState>
        get() { return _state }

    // UI Events
    val onClickSearchIcon = {
        _state.value.showSearchTextField.value = !_state.value.showSearchTextField.value
    }

    val onSearchTextChanged: (String) -> Unit = {}
    val onClickMessage: (Message) -> Unit = { doOpenMessagesInThreadFromChildFragment?.invoke(it) }

    /**
     * Invoked when [MessagesFromFragment] needs to be opened
     */
    var doOpenMessagesInThreadFromChildFragment: ((Message) -> Unit)? = null

    var childUserEmail: String? = null
        set(value) {
            field = value
            _state.value.title.value = childUserEmail ?: ""
        }

    fun getLastSmsInfoOfEachChild(childUserId: String) {
        viewModelScope.launch {
            childSmsInfoRepository.getLastSmsInfoOfChild(childUserId).collect { childSmsInfoList ->
                _state.value.lastMessageList.addAll(childSmsInfoList.map {
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
            }
        }
    }

}