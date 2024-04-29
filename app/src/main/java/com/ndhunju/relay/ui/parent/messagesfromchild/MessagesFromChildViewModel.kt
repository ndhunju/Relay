package com.ndhunju.relay.ui.parent.messagesfromchild

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ndhunju.relay.data.ChildSmsInfoRepository
import com.ndhunju.relay.ui.Screen
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.extensions.asState
import com.ndhunju.relay.util.worker.SyncChildMessagesWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MessagesFromChildViewModel(
    private val workManager: WorkManager,
    private val childSmsInfoRepository: ChildSmsInfoRepository
): ViewModel() {

    private val _isRefreshing = mutableStateOf(false)
    val isRefresh = _isRefreshing.asState()

    /**
     * Set to true to show a circular progress indicator to indicate
     * that the app is doing some work in the background
     */
    private val _showProgress = mutableStateOf(true)
    val showProgress = _showProgress.asState()

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
            doOpenMessagesInThreadFromChildScreen?.invoke(childUserId, message)
        }
    }

    val onRefreshByUser = {
        // Queue SyncChildMessagesWorker to fetch new Child Messages, if any
        SyncChildMessagesWorker.doSyncChildMessagesFromServer(workManager)
        getLastSmsInfoOfEachChild()
    }

    //endregion

    /**
     * Invoked when [Screen.MessagesInThreadFromChild] needs to be opened.
     * It passes child user id and [Message] object
     */
    var doOpenMessagesInThreadFromChildScreen: ((String, Message) -> Unit)? = null

    var childUserPhone: String? = null
        set(value) {
            field = value
            _title.value = childUserPhone ?: ""
        }

    lateinit var childUserId: String

    fun getLastSmsInfoOfEachChild() {
        viewModelScope.launch(Dispatchers.IO) {
            _showProgress.value = true
            awaitTillLastSyncChildMessagesWorkerIsFinished()
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
                _showProgress.value = false
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Awaits till the last [SyncChildMessagesWorker] is finished. Returns true
     * if it was finished. Otherwise, false if it timed out before finishing.
     */
    private suspend fun awaitTillLastSyncChildMessagesWorkerIsFinished(): Boolean {
        val channel = Channel<Int>()
        val liveData = workManager.getWorkInfosByTagLiveData(SyncChildMessagesWorker.TAG)
        // observeForever needs to be called on Main thread
        withContext(Dispatchers.Main) {
            //delay(3000)
            liveData.observeForever(object : Observer<List<WorkInfo>> {
                override fun onChanged(value: List<WorkInfo>) {
                    if (value[value.lastIndex].state.isFinished) {
                        liveData.removeObserver(this)
                        channel.trySend(1)
                    }
                }
            })
        }
        val received = withTimeoutOrNull(11_000) {
            // The control remains here until time out
            // or a new value is sent to this channel with trySend()
            channel.receive()
        }

        return received == 1 // Return true if it did not time out
    }

}