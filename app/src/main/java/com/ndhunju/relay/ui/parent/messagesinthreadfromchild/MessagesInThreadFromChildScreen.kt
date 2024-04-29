package com.ndhunju.relay.ui.parent.messagesinthreadfromchild

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.ui.messagesfrom.MessagesFromView

@Composable
fun MessagesInThreadFromChildScreen(
    viewModel: MessagesInThreadFromChildVM,
    onClickSend: (() -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null
) {
    val uiState = viewModel.messageFromUiState.collectAsStateWithLifecycle()
    // Show progress if loading
    if (uiState.value.isLoading.value) {
        Box(
            modifier = Modifier
                .wrapContentSize(align = Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = uiState.value.isLoading.value.not(),
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = 150,
                easing = LinearOutSlowInEasing
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutLinearInEasing
            )
        )
    ) {
        MessagesFromView(
            viewModel.senderAddress,
            uiState.value.messagesInThread,
            uiState.value.isLoading.value,
            onClickSend = onClickSend,
            onBackPressed = onBackPressed
        )
    }
}