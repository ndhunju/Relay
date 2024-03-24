package com.ndhunju.relay.ui.parent.messagesfromchild

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.util.extensions.asState
import com.ndhunju.relay.util.worker.SyncChildMessagesWorker
import kotlinx.coroutines.launch

class MessagesFromChildViewModel(
    private val workManager: WorkManager,
    private val childSmsInfoRepository: ChildSmsInfoRepository
): ViewModel() {

    private val _isRefreshing = mutableStateOf(false)
    val isRefresh = _isRefreshing.asState()

    private val _showSearchTextField = mutableStateOf(false)
    var showSearchTextField: State<Boolean> = _showSearchTextField

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _lastMessageForEachThread  = mutableStateListOf<Message>()
    val lastMessageForEachThread: SnapshotStateList<Message> = _lastMessageForEachThread

    //region UI Events
    val onClickSearchIcon = {
        _showSearchTextField.value = _showSearchTextField.value.not()
    }

    val onSearchTextChanged: (String) -> Unit = {}
    val onClickMessage: (Message) -> Unit = { message ->
        childUserId.let { childUserId ->
            doOpenMessagesInThreadFromChildFragment?.invoke(childUserId, message)
        }
    }

    val onRefreshByUser = {
        val workRequestId = SyncChildMessagesWorker.doSyncChildMessagesFromServer(workManager)
        workManager.getWorkInfoByIdFlow(workRequestId).asLiveData().observeForever { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                getLastSmsInfoOfEachChild()
            }
        }
    }

    //endregion

    /**
     * Invoked when [MessagesFromFragment] needs to be opened.
     * It passes child user id and [Message] object
     */
    var doOpenMessagesInThreadFromChildFragment: ((String, Message) -> Unit)? = null

    var childUserEmail: String? = null
        set(value) {
            field = value
            _title.value = childUserEmail ?: ""
        }

    lateinit var childUserId: String

    fun getLastSmsInfoOfEachChild() {
        viewModelScope.launch {
            childSmsInfoRepository.getLastSmsInfoOfChild(childUserId).collect { childSmsInfoList ->
                _lastMessageForEachThread.clear()
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
                _isRefreshing.value = false
            }
        }
    }

}