package com.ndhunju.relay

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Preview
@Composable
fun RelaySmsAppPreview() {
    val viewModel = RelaySmsViewModel()
    RelaySmsApp(viewModel)
}

@Composable
fun RelaySmsApp(
    viewModel: RelaySmsViewModel
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    Surface(modifier = Modifier.fillMaxWidth()) {
        RelaySmsAppBar(
            viewState.showSearchTextField,
            viewModel.onClickSearchIcon,
            viewModel.onSearchTextChanged,
            viewModel.onClickAccountIcon
        )
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
        modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top),
        title = {
            Row (verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(id = R.string.image_description_app_logo)
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

@Composable
fun SearchTextField(onSearchTextChanged: (String) -> Unit) {
    Row(
        modifier = Modifier.padding(0.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(value = "", onValueChange = onSearchTextChanged)
        Icon(
            Icons.Rounded.Search,
            contentDescription = stringResource(id = R.string.image_description_search)
        )
    }
}

