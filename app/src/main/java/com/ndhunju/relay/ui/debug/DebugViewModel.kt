package com.ndhunju.relay.ui.debug

import androidx.lifecycle.ViewModel

class DebugViewModel: ViewModel() {

    var onClickForceCrashItem: (() -> Unit) = {
        throw RuntimeException("Test Crash")
    }
}