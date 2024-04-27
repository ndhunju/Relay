package com.ndhunju.relay.ui.messagesfrom

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.ScrollToTopLaunchedEffect
import com.ndhunju.relay.ui.custom.CenteredMessageWithButton
import com.ndhunju.relay.ui.custom.SyncStatusIcon
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.fakeMessages
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.theme.LocalColors
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.extensions.bounceOnClick

@SuppressLint("UnrememberedMutableState")
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessagesFromPreview() {
    RelayTheme {
        MessagesFromView(
            fakeMessages.first().from,
            messageList = mutableStateListOf<Message>().apply { addAll(fakeMessages) },
            isLoading = false
        )
    }
}

@Composable
fun MessagesFromView(
    senderAddress: String?,
    messageList: List<Message>,
    isLoading: Boolean,
    text: State<String>? = null,
    onTextMessageChanged: ((String) -> Unit)? = null,
    onClickSend: (() -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null
) {
    // This coroutine is bound to the lifecycle of the enclosing compose
    //val composeCoroutine = rememberCoroutineScope()
    if (isLoading) {
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
    }

    AnimatedVisibility(
        visible = isLoading.not(),
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
        MessagesFromViewCore(
            senderAddress,
            messageList,
            text,
            onTextMessageChanged,
            onClickSend,
            onBackPressed
        )
    }
}

@Composable
fun MessagesFromViewCore(
    senderAddress: String?,
    messageList: List<Message>,
    text: State<String>? = null,
    onTextMessageChanged: ((String) -> Unit)? = null,
    onClickSend: (() -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null
) {
    // If for some reason no sender is passed, show error message
    if (senderAddress?.isEmpty() == true && messageList.isEmpty()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBarWithUpButton(senderAddress, onBackPressed) }
        ) { innerPadding ->
            CenteredMessageWithButton(
                Modifier.padding(innerPadding),
                message = stringResource(R.string.msg_no_sender)
            )
        }
    } else {
        // Using a separate Scaffold so that the error message could be centered on the screen
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBarWithUpButton(senderAddress, onBackPressed) },
            bottomBar = { SendMessageBar(text, onTextMessageChanged, onClickSend) }
        ) { internalPadding ->

            val listState = rememberLazyListState()
            ScrollToTopLaunchedEffect(messageList, listState)

            LazyColumn(
                state = listState,
                contentPadding = internalPadding,
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxSize(),
                content = {
                    // Show list of messages for the given thread
                    itemsIndexed(
                        items = messageList,
                        // Pass key for better performance like setHasStableIds
                        key = { _, message -> message.idInAndroidDb },
                        itemContent = { i: Int, message: Message ->
                            ChatBubbleView(
                                message = message,
                                previous = messageList.getOrNull(i - 1),
                                nextMessage = messageList.getOrNull(i + 1)
                            )
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun ChatBubbleView(
    modifier: Modifier = Modifier,
    message: Message,
    previous: Message? = null,
    nextMessage: Message? = null,
) {
    // Determine what corner radius and padding values to use for current message
    val isSameUserAsBefore = previous?.isSentByUser() == message.isSentByUser()
    val isSameUserAsNext = nextMessage?.isSentByUser() == message.isSentByUser()
    val topStartCorner = if (isSameUserAsBefore) 3.dp else 11.dp
    val topEndCorner = if (isSameUserAsBefore) 3.dp else 11.dp
    val bottomStartCorner = if (isSameUserAsNext) 3.dp else 11.dp
    val bottomEndCorner = if (isSameUserAsNext) 3.dp else 11.dp
    val topPadding = if (isSameUserAsBefore) 0.5.dp else 7.dp
    val bottomPadding = if (isSameUserAsNext) 0.5.dp else 7.dp

    Row(
        modifier = modifier
            .padding(horizontal = LocalDimens.current.contentPaddingHorizontal)
            .padding(top = bottomPadding, bottom = topPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // If the message is sent by the user, show the message on the right/end side.
        // Otherwise, show it on the left/start side.
        if (message.isSentByUser()) {
            Spacer(modifier = Modifier.weight(0.2f))
        }

        Row(
            modifier = Modifier
                .wrapContentSize(
                    align = if (message.isSentByUser()) {
                        Alignment.TopEnd
                    } else {
                        Alignment.TopStart
                    }
                )
                .weight(0.8f),
            horizontalArrangement = Arrangement.spacedBy(0.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (message.isSentByUser()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        shape = RoundedCornerShape(
                            topStart = bottomStartCorner,
                            topEnd = bottomEndCorner,
                            bottomStart = topStartCorner,
                            bottomEnd = topEndCorner
                        )
                    )
                    .padding( // Inner Padding
                        vertical = LocalDimens.current.itemPaddingVertical.div(2),
                        horizontal = 8.dp
                    )
                    // Setting fill=false prevents second item
                    // in the Row to get squeezed to width 0
                    .weight(weight = 1F, fill = false)
            ) {
                Text(
                    text = message.body, //+ "\n" + message.toString(), for debugging
                    // Since the backgrounds are primary and tertiary, setting
                    // text colors to onPrimary and onTertiary for contrast
                    color = if (message.isSentByUser()) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (message.isSentByUser().not()) {
                SyncStatusIcon(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    syncStatus = message.syncStatus
                )
            }
        }

        if (message.isSentByUser().not()) {
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }

}

@Preview
@Composable
@SuppressLint("UnrememberedMutableState")
fun SendMessageBarPreviewLong() {
    RelayTheme {
        SendMessageBar(
            text = mutableStateOf(
                "This is a test message with a long message extending to second paragraph."
            )
        )
    }
}

@Composable
fun SendMessageBar(
    text: State<String>? = null,
    onTextMessageChanged: ((String) -> Unit)? = null,
    onClickSend: (() -> Unit)? = null,
) {
    // Add text?.value as the key, so that remember is re-executed when that key changes
    // Ideally, we could have just use text?.value directly but if we don't do this
    // the performTextReplacement won't fill this text field during ui test
    val currentText = remember(text?.value) { mutableStateOf(text?.value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            .padding(
                horizontal = LocalDimens.current.contentPaddingHorizontal,
                vertical = LocalDimens.current.itemPaddingVertical
            )
            .imePadding() // Not working as intended though
    ) {
        BasicTextField(
            value = currentText.value ?: "",
            modifier = Modifier
                .weight(1f) // Make this field fill the remaining space
                .align(Alignment.CenterVertically)
                .background(
                    LocalColors.current.textFieldBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            onValueChange = {
                onTextMessageChanged?.invoke(it)
                currentText.value = it
            },
        )

        Spacer(
            modifier = Modifier.size(
                width = LocalDimens.current.contentPaddingHorizontal.div(2),
                height = 1.dp
            )
        )

        Icon(
            imageVector = Icons.AutoMirrored.Default.Send,
            contentDescription = stringResource(R.string.description_send_message),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .bounceOnClick()
                .clickable(
                    onClickLabel = stringResource(R.string.click_label_sends_message),
                    onClick = { onClickSend?.invoke() },
                )
        )
    }
}