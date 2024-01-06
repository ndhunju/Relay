package com.ndhunju.relay.ui.messagesfrom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
                // TODO: Nikesh - Arrange items from bottom up
                LazyColumn(
                    contentPadding = internalPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        // Show list of messages for given sender
                        itemsIndexed(messageList) { _: Int, message: Message ->
                            Box(
                                Modifier
                                    .padding(
                                        start = LocalDimens.current.contentPaddingHorizontal,
                                        end = LocalDimens.current.contentPaddingHorizontal * 3,
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(size = 9.dp)
                                    )
                                    .padding(
                                        vertical = LocalDimens.current.itemPaddingVertical,
                                        horizontal = 8.dp
                                    )
                            ) {
                                Text(
                                    text = message.body,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    })
            }
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
                    contentDescription = "Back"
                )
            }
        }
    )
}