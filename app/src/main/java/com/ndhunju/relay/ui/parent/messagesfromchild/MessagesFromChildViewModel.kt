package com.ndhunju.relay.ui.parent.messagesfromchild

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import kotlinx.coroutines.launch

class MessagesFromChildViewModel(
    private val childSmsInfoRepository: ChildSmsInfoRepository
): ViewModel() {

    private val _showSearchTextField = mutableStateOf(false)
    var showSearchTextField: State<Boolean> = _showSearchTextField

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _lastMessageForEachThread  = mutableStateListOf<Message>()
    val lastMessageForEachThread: SnapshotStateList<Message> = _lastMessageForEachThread

    // UI Events
    val onClickSearchIcon = {
        _showSearchTextField.value = _showSearchTextField.value.not()
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
            _title.value = childUserEmail ?: ""
        }

    fun getLastSmsInfoOfEachChild(childUserId: String) {
        viewModelScope.launch {
            childSmsInfoRepository.getLastSmsInfoOfChild(childUserId).collect { childSmsInfoList ->
                _lastMessageForEachThread.addAll(childSmsInfoList.map {
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