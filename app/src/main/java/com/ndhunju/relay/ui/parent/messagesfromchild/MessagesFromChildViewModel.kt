package com.ndhunju.relay.ui.parent.messagesfromchild

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

    private var _state = MutableStateFlow(MainScreenUiState())
    val state: StateFlow<MainScreenUiState>
        get() { return _state }

    // UI Events
    val onClickSearchIcon = {
        _state.value.showSearchTextField = !_state.value.showSearchTextField
    }

    val onSearchTextChanged: (String) -> Unit = {}
    val onClickMessage: (Message) -> Unit = { doOpenMessageFromFragment?.invoke(it) }


    /**
     * Invoked when [MessagesFromFragment] needs to be opened
     */
    var doOpenMessageFromFragment: ((Message) -> Unit)? = null

    var childUserEmail: String? = null
        set(value) {
            field = value
            _state.value.title.value = childUserEmail ?: ""
        }

    fun getLastSmsInfoOfEachChild(childUserId: String) {
        viewModelScope.launch {
            // TODO: Nikesh - Add logic to get last messages from the given child
            _state.value = MainScreenUiState(mockMessages)
        }
    }

}