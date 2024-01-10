package com.ndhunju.relay.ui.messagesfrom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.SyncStatusIcon
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.mockMessages
import com.ndhunju.relay.ui.theme.LocalDimens

@Preview
@Composable
fun MessagesFromPreview() {
    return MessagesFromView(mockMessages.first().from, messageList = mockMessages)
}

@Composable
fun MessagesFromView(
    senderAddress: String,
    messageList: List<Message>,
    onBackPressed: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        // If for some reason no sender is passed, show error message
        if (senderAddress.isEmpty() && messageList.isEmpty()) {
            Scaffold(
                topBar = { TopAppBarWithUpButton(senderAddress, onBackPressed) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = LocalDimens.current.contentPaddingHorizontal),
                ) {
                    Text(
                        text = stringResource(R.string.msg_no_sender_found),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        } else {
            // Using a separate Scaffold so that the error message
            // could be centered on the screen
            Scaffold(
                topBar = {
                    TopAppBarWithUpButton(senderAddress, onBackPressed)
                }
            ) { internalPadding ->
                LazyColumn(
                    contentPadding = internalPadding,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        // Show list of messages for the given thread
                        itemsIndexed(messageList) { _: Int, message: Message ->
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = LocalDimens.current.contentPaddingHorizontal
                                )
                            ) {
                                ChatBubbleView(message = message)
                            }
                        }
                    })
            }
        }

    }
}

@Composable
fun ChatBubbleView(message: Message) {
    Row {
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
                .weight(0.8f)
        )  {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(size = 11.dp)
                    )
                    .padding( // Inner Padding
                        vertical = LocalDimens.current.itemPaddingVertical,
                        horizontal = 8.dp
                    )
                    // Setting fill=false prevents second item
                    // in the Row to get squeezed to width 0
                    .weight(weight = 1F, fill = false)
            ) {
                Text(
                    text = message.body, //+ "\n" + message.toString(), for debugging
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = if (message.isSentByUser()) {
                        TextAlign.End
                    } else {
                        TextAlign.Start
                    }
                )
            }

            if (message.isSentByUser().not()) {
                SyncStatusIcon(
                    syncStatus = message.syncStatus,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
        }

        if (message.isSentByUser().not()) {
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopAppBarWithUpButton(senderAddress: String, onBackPressed: (() -> Unit)?) {
    TopAppBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        title = {
            Text(
                text = senderAddress,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackPressed?.invoke() }) {
                Icon(
                    modifier = Modifier.padding(4.dp),
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.image_description_go_back)
                )
            }
        }
    )
}