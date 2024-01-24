package com.ndhunju.relay.ui.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.mockChildUsers
import com.ndhunju.relay.ui.theme.LocalDimens

@Preview
@Composable
fun ChildUserListScreenPreview() {
    ChildUserListScreen(mockChildUsers, onClickChildUser = {}, onUpPressed = {})
}

@Composable
fun ChildUserListScreen(
    childUsers: List<Child>,
    onClickChildUser: (Child) -> Unit,
    onUpPressed: (() -> Unit)?
) {
    Scaffold(
        topBar = {
            TopAppBarWithUpButton(
                title = stringResource(R.string.child_user_screen_title),
                onUpPressed = onUpPressed
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            content = {
                itemsIndexed(childUsers, key = { _, item -> item.id}) { _: Int, childUser: Child ->
                    Row(modifier = Modifier
                        .padding(
                            vertical = LocalDimens.current.itemPaddingVertical,
                            horizontal = LocalDimens.current.contentPaddingHorizontal
                        )
                        .clickable { onClickChildUser(childUser) }
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

                        Text(text = childUser.email, modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterVertically)
                        )
                    }
                    Divider()
                }
            }
        )
    }
}

data class Child(val id: String, val email: String)