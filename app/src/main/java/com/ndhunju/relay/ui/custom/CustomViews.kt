package com.ndhunju.relay.ui.custom

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.theme.LocalColors
import com.ndhunju.relay.ui.theme.RelayTheme

@Composable
fun SearchTextField(onSearchTextChanged: ((String) -> Unit)? = null) {
    Row(
        modifier = Modifier.padding(0.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(value = "", onValueChange = onSearchTextChanged ?: {})
        Icon(
            Icons.Rounded.Search,
            contentDescription = stringResource(id = R.string.image_description_search)
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SyncStatusIconPreview() {
    RelayTheme {
        SyncStatusIcon()
    }
}


@Composable
fun SyncStatusIcon(modifier: Modifier = Modifier, syncStatus: Result<Void>? = Result.Success()) {
    Icon(
        painter = painterResource(R.drawable.baseline_sync_status_24),
        contentDescription = stringResource(R.string.image_description_sync_status_logo),
        tint = when (syncStatus) {
            is Result.Pending -> Color.LightGray
            is Result.Success -> LocalColors.current.success
            is Result.Failure -> LocalColors.current.failure
            // Hide the icon by making it have same color as the background
            else -> MaterialTheme.colorScheme.background
        },
        modifier = modifier
            .padding(start = 4.dp)
            .size(16.dp)
            .alpha(if (syncStatus == null) 0f else 1f)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBarWithUpButton(
    title: String?,
    onUpPressed: (() -> Unit)?,
    showUpButton: Boolean = true) {
    TopAppBar(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .shadow(6.dp),
        title = {
            Text(
                text = title ?: "",
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            if (showUpButton) {
                IconButton(onClick = { onUpPressed?.invoke() }) {
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.image_description_go_back)
                    )
                }
            }
        }
    )
}