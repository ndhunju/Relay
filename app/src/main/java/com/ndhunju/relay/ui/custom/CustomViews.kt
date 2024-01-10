package com.ndhunju.relay.ui.custom

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.service.Result

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

@Composable
fun SyncStatusIcon(syncStatus: Result?, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.baseline_sync_status_24),
        contentDescription = stringResource(R.string.image_description_sync_status_logo),
        tint = when (syncStatus) {
            is Result.Pending -> Color.LightGray
            is Result.Success -> Color.Green
            is Result.Failure -> MaterialTheme.colorScheme.error
            // Hide the icon by making it have same color as the background
            else -> MaterialTheme.colorScheme.background
        },
        modifier = modifier
            .padding(start = 8.dp)
            .size(16.dp)
            .alpha(if (syncStatus == null) 0f else 1f)
    )
}