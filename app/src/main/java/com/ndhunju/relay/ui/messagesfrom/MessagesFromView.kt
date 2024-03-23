package com.ndhunju.relay.ui.messagesfrom

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.CenteredMessageWithButton
import com.ndhunju.relay.ui.custom.SyncStatusIcon
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.mockMessages
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessagesFromPreview() {
    RelayTheme {
        MessagesFromView(mockMessages.first().from, messageList = mockMessages)
    }
}

@Composable
fun MessagesFromView(
    senderAddress: String?,
    messageList: List<Message>,
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
                stringResource(R.string.msg_no_sender)
            )
        }
    } else {
        // Using a separate Scaffold so that the error message could be centered on the screen
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBarWithUpButton(senderAddress, onBackPressed) }
        ) { internalPadding ->
            LazyColumn(
                contentPadding = internalPadding,
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxSize(),
                content = {
                    // Show list of messages for the given thread
                    itemsIndexed(messageList) { i: Int, message: Message ->
                        ChatBubbleView(
                            message = message,
                            previous = messageList.getOrNull(i - 1),
                            nextMessage = messageList.getOrNull(i + 1)
                        )
                    }
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