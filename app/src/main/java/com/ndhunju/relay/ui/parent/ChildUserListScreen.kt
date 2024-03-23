package com.ndhunju.relay.ui.parent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.SimpleAlertDialog
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.mockChildUsers
import com.ndhunju.relay.ui.theme.LocalDimens
import kotlinx.coroutines.flow.MutableStateFlow

@Preview
@Composable
fun ChildUserListScreenPreview() {
    ChildUserListScreen(
        showProgress = remember { mutableStateOf(false) },
        childUsers = MutableStateFlow(mockChildUsers).collectAsState(),
        showPostNotificationPermissionDialog = remember { mutableStateOf(false) }
    )
}

@Composable
fun ChildUserListScreen(
    viewModel: ChildUserListViewModel,
    onUpPressed: (() -> Unit)? = null,
    ) {
    ChildUserListScreen(
        viewModel.showProgress.collectAsState(),
        viewModel.childUsers.collectAsState(),
        viewModel.showPostNotificationPermissionDialog,
        viewModel::onClickChildUser,
        viewModel::onClickAddChildKey,
        viewModel::onClickAllowNotificationDialogBtnOk,
        viewModel::onClickAllowNotificationDialogBtnCancel,
        onUpPressed
    )
}


@Composable
fun ChildUserListScreen(
    showProgress: State<Boolean>,
    childUsers: State<List<Child>>,
    showPostNotificationPermissionDialog: State<Boolean>,
    onClickChildUser: ((Child) -> Unit)? = null,
    onClickAddChildKey: ((Child) -> Unit)? = null,
    onClickAllowNotificationDialogBtnOk: (() -> Unit)? = null,
    onClickAllowNotificationDialogBtnCancel: (() -> Unit)? = null,
    onUpPressed: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            TopAppBarWithUpButton(
                title = stringResource(R.string.child_user_screen_title),
                onUpPressed = onUpPressed
            )
        }
    ) { innerPadding ->

        if (showProgress.value) {
            LoadingIndicator()
        }

        AnimatedVisibility(visible = showProgress.value.not(), enter = fadeIn()) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                content = {
                    itemsIndexed(childUsers.value, key = { _, item -> item.id })
                    { _: Int, childUser: Child ->
                        ChildUserColumnItem(onClickChildUser, onClickAddChildKey, childUser)
                        Divider()
                    }
                }
            )
        }
    }
}

@Composable
private fun ChildUserColumnItem(
    onClickChildUser: ((Child) -> Unit)? = null,
    onClickAddChildKey: ((Child) -> Unit)? = null,
    childUser: Child
) {
    Row(modifier = Modifier
        .padding(
            vertical = LocalDimens.current.itemPaddingVertical,
            horizontal = LocalDimens.current.contentPaddingHorizontal
        )
        .clickable { onClickChildUser?.invoke(childUser) }
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(
                R.string.image_description_child_user_icon
            ),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 6.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.baseline_key_24),
            contentDescription = stringResource(R.string.content_description_add_encryption_key),
            tint = colorResource(if (childUser.encKey == null) R.color.failure else R.color.success),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 6.dp)
                .clickable { onClickAddChildKey?.invoke(childUser) }
        )

        Text(
            text = childUser.email, modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center).size(32.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

data class Child(val id: String, val email: String, val encKey: String? = null)