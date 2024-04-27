package com.ndhunju.relay.ui.custom

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope

@Composable
fun ScrollToTopLaunchedEffect(key: Any?, state: LazyListState) {
    LaunchedEffect(key1 = key) {
        snapshotFlow { state.firstVisibleItemIndex == 0 }
            .collect { state.animateScrollToItem(0) }
    }
}

@Composable
fun LaunchedEffectOnce(block: CoroutineScope.() -> Unit) {
    LaunchedEffect(Unit, block)
}