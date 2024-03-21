package com.ndhunju.relay.ui.custom

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.theme.LocalColors
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme

@Preview
@Composable
fun SearchTextFieldPreview() {
    RelayTheme {
        SearchTextField()
    }
}

@Composable
fun SearchTextField(onSearchTextChanged: ((String) -> Unit)? = null) {

    var text by rememberSaveable { mutableStateOf("") }

    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(16.dp))
            .padding(8.dp),
        value = text,
        onValueChange = {
            onSearchTextChanged?.invoke(it)
            text = it
        },
        singleLine = true
    )
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

@Preview
@Composable
private fun CenteredTextPreview() {
    RelayTheme {
        CenteredText(modifier = Modifier, string = "This text should be centered on the screen.")
    }
}

@Composable
fun CenteredText(modifier: Modifier, string: String) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(horizontal = LocalDimens.current.contentPaddingHorizontal),
    ) {
        Text(
            text = string,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}