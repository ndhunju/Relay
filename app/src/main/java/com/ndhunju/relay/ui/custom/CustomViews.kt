package com.ndhunju.relay.ui.custom

import android.content.res.Configuration
import android.view.View.GONE
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
    var isPositioned by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(16.dp))
            .padding(8.dp)
            .focusRequester(focusRequester)
            .onGloballyPositioned {
                if (isPositioned.not()) {
                    isPositioned = true
                    focusRequester.requestFocus()
                }
            },
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
fun SyncStatusIcon(modifier: Modifier = Modifier, syncStatus: Result<Nothing>? = Result.Success()) {
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

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBarWithUpButton(
    title: String? = null,
    onUpPressed: (() -> Unit)? = null,
    showUpButton: Boolean = true
) {
    Column {
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.image_description_go_back)
                        )
                    }
                }
            }
        )

        CriticalMessageBar()
    }
}

/**
 * Shows a critical message with had to miss visual.
 * One can update this view by find it by id [R.id.critical_message_text_view]
 */
@Composable
fun CriticalMessageBar() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            TextView(context).apply {
                // Set ID so that it could updated from activity or fragment
                // whenever there is a broadcast of critical message
                id = R.id.critical_message_text_view
                textAlignment = TEXT_ALIGNMENT_CENTER
                setBackgroundColor(context.getColor(R.color.failure))
                setTextColor(Color.White.toArgb())
                visibility = GONE
            }
        }
    )
}

@Preview
@Composable
private fun CenteredTextPreview() {
    RelayTheme {
        Surface {
            CenteredMessageWithButton(
                modifier = Modifier,
                message = "This text should be centered on the screen.",
                buttonText = "Action Button"
            )
        }
    }
}

/**
 * Centers the [message] and [buttonText] on the screen
 */
@Composable
fun CenteredMessageWithButton(
    modifier: Modifier,
    message: String? = null,
    buttonText: String? = null,
    onClickButton: (() -> Unit)? = null,
    ) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = LocalDimens.current.contentPaddingHorizontal.times(2)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (message != null) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (buttonText != null) {
            Button(
                onClick = { onClickButton?.invoke() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = buttonText)
            }
        }
    }
}