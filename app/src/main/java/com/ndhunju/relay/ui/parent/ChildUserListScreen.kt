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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterfaceDummyImpl
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.util.InMemoryCurrentUser

@Preview
@Composable
fun ChildUserListScreenPreview() {
    ChildUserListScreen(ChildUserListViewModel(
        ApiInterfaceDummyImpl,
        WorkManager.getInstance(LocalContext.current),
        InMemoryCurrentUser(),
    ))
}

@Composable
fun ChildUserListScreen(
    viewModel: ChildUserListViewModel,
    onUpPressed: (() -> Unit)? = null
) {
    val childUsers = viewModel.childUsers.collectAsState()
    Scaffold(
        topBar = {
            TopAppBarWithUpButton(
                title = stringResource(R.string.child_user_screen_title),
                onUpPressed = onUpPressed
            )
        }
    ) { innerPadding ->

        val showProgress = viewModel.showProgress.collectAsState()

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
                        ChildUserColumnItem(
                            viewModel::onClickChildUser,
                            viewModel::onClickAddChildKey,
                            childUser
                        )
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
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(32.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

data class Child(val id: String, val email: String, val encKey: String? = null)