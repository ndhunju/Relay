package com.ndhunju.relay.ui.custom

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R

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