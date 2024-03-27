package com.ndhunju.relay.ui.custom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme

@Preview
@Composable
private fun AlertDialogPreview() {
    RelayTheme {
        MessageAlertDialog(message = "This is a simple message")
    }
}

@Composable
fun AppUpdateDialog(
    onClickUpdate: () -> Unit
) {
    RelayTheme {
        SimpleAlertDialog(
            positiveButtonText = stringResource(R.string.screen_app_update_positive_btn),
            onClickDialogBtnPositive = onClickUpdate
        ) {
            Text(
                modifier = Modifier.padding(vertical = LocalDimens.current.itemPaddingVertical),
                text = stringResource(id = R.string.screen_app_update_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(text = stringResource(R.string.screen_app_update_body))
        }
    }
}

@Composable
fun MessageAlertDialog(
    message: String? = null,
    onClickDialogBtnOk: (() -> Unit)? = null,
    onClickDialogBtnCancel: (() -> Unit)? = null,
    dialogProperties: DialogProperties = DialogProperties(dismissOnClickOutside = false)
) {
    CustomContentAlertDialog(onClickDialogBtnOk, onClickDialogBtnCancel, dialogProperties) {
        if (message != null) {
            Text(text = message)
        }
    }
}

@Composable
fun CustomContentAlertDialog(
    onClickDialogBtnOk: (() -> Unit)? = null,
    onClickDialogBtnCancel: (() -> Unit)? = null,
    dialogProperties: DialogProperties = DialogProperties(dismissOnClickOutside = false),
    customContent: @Composable (ColumnScope.() -> Unit)
) {
    SimpleAlertDialog(
        positiveButtonText = stringResource(id = R.string.ok),
        negativeButtonText = stringResource(id = R.string.cancel),
        onClickDialogBtnPositive = onClickDialogBtnOk,
        onClickDialogBtnNegative = onClickDialogBtnCancel,
        dialogProperties = dialogProperties,
        customContent = customContent
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SimpleAlertDialog(
    positiveButtonText: String? = null,
    negativeButtonText: String? = null,
    onClickDialogBtnPositive: (() -> Unit)? = null,
    onClickDialogBtnNegative: (() -> Unit)? = null,
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
                    if (negativeButtonText != null) {
                        TextButton(onClick = { onClickDialogBtnNegative?.invoke() }) {
                            Text(text = negativeButtonText)
                        }
                    }

                    if (positiveButtonText != null) {
                        TextButton(onClick = { onClickDialogBtnPositive?.invoke() }) {
                            Text(text = positiveButtonText)
                        }
                    }
                }
            }
        }
    }
}