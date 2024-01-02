package com.ndhunju.relay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RelaySmsViewModel {

    private var _state = MutableStateFlow(SmsReporterViewState())
    val state: StateFlow<SmsReporterViewState>
        get() { return _state }

    var onClickSearchIcon = {
        _state.value.showSearchTextField = !_state.value.showSearchTextField
    }

    var onSearchTextChanged: (String) -> Unit = {}
    var onClickAccountIcon = {}

}

data class SmsReporterViewState(
    var showSearchTextField: Boolean = false
)