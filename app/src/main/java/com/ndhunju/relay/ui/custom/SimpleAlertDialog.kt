package com.ndhunju.relay.ui.custom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme

@Preview
@Composable
private fun SimpleAlertDialogPreview() {
    RelayTheme {
        SimpleAlertDialog(message = "This is a simple message")
    }
}


@Composable
fun SimpleAlertDialog(
    message: String? = null,
    onClickDialogBtnOk: (() -> Unit)? = null,
    onClickDialogBtnCancel: (() -> Unit)? = null,
    dialogProperties: DialogProperties = DialogProperties(dismissOnClickOutside = false)
) {
    SimpleAlertDialog(onClickDialogBtnOk, onClickDialogBtnCancel, dialogProperties) {
        if (message != null) {
            Text(text = message)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SimpleAlertDialog(
    onClickDialogBtnOk: (() -> Unit)? = null,
    onClickDialogBtnCancel: (() -> Unit)? = null,
    dialogProperties: DialogProperties = DialogProperties(dismissOnClickOutside = false),
    customContent: @Composable (ColumnScope.() -> Unit)
) {
    AlertDialog(onDismissRequest = {}, properties = dialogProperties) {
        Surface {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = LocalDimens.current.contentPaddingHorizontal,
                        vertical = LocalDimens.current.itemPaddingVertical
                    )
            ) {

                customContent()

                Row(
                    Modifier
                        .align(Alignment.End)
                        .padding(top = LocalDimens.current.itemPaddingVertical),
                ) {
                    TextButton(
                        onClick = { onClickDialogBtnCancel?.invoke() }
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(
                        onClick = { onClickDialogBtnOk?.invoke() }
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}