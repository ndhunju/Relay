package com.ndhunju.relay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.data.RelayRepository
import com.ndhunju.relay.ui.custom.SearchTextField
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messages.MessageListItem
import com.ndhunju.relay.ui.theme.LocalDimens

@Preview
@Composable
fun RelaySmsAppPreview() {
    val viewModel = RelaySmsViewModel(RelayRepository(LocalContext.current))
//    viewModel.state.value.showErrorMessageForPermissionDenied = true
    RelaySmsApp(viewModel)
}

@Composable
fun RelaySmsApp(
    viewModel: RelaySmsViewModel
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            RelaySmsAppBar(
                viewState.showSearchTextField,
                viewModel.onClickSearchIcon,
                viewModel.onSearchTextChanged,
                viewModel.onClickAccountIcon
            )
        },

    ) { innerPadding ->
        if (viewState.showErrorMessageForPermissionDenied) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = LocalDimens.current.contentPaddingHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.permission_rationale_sms_read_send),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.onClickGrantPermission() },
                    modifier = Modifier.padding(16.dp)
                    ) {
                    Text(text = stringResource(R.string.grant_permissions))
                }
            }

        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                content = {
                    itemsIndexed(viewState.messages) { _: Int, message: Message ->
                        MessageListItem(message, true, viewModel.onClickMessage)
                    }
                })
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaySmsAppBar(
    showSearchTextField: Boolean = false,
    onClickSearchIcon: () -> Unit = {},
    onSearchTextChanged: (String) -> Unit = {},
    onClickAccountIcon: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        title = {
            Row (verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(id = R.string.image_description_app_logo),
                    modifier = Modifier.padding(end = 8.dp)
                )
                if (showSearchTextField) {
                    SearchTextField(onSearchTextChanged = onSearchTextChanged)
                } else {
                    Text(text = stringResource(id = R.string.app_name))
                }
            }
        },
        actions = {
            IconButton(onClick = onClickSearchIcon ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(id = R.string.image_description_search)
                )
            }
            IconButton(onClick = onClickAccountIcon ) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = stringResource(id = R.string.image_description_search)
                )
            }
        }
    )
}

