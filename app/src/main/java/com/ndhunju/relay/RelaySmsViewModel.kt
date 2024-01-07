package com.ndhunju.relay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ndhunju.relay.data.RelayRepository
import com.ndhunju.relay.ui.messages.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RelaySmsViewModel(
   repository: RelayRepository
) {

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
        state.value.messages = repository.getLastSmsBySender()
        // Reset this value in case it was set to true earlier
        state.value.showErrorMessageForPermissionDenied = false
    }

    var onNewSmsReceived = {

    }

}

data class SmsReporterViewState(
    var messages: List<Message> = emptyList()
) {
    // Note: Compose doesn't track inner fields for change unless we use mutableStateOf
    var showErrorMessageForPermissionDenied: Boolean by mutableStateOf(false)
    var showSearchTextField: Boolean by mutableStateOf(false)
}