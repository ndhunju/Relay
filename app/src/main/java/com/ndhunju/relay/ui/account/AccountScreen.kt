package com.ndhunju.relay.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.RelayOutlinedTextField
import com.ndhunju.relay.ui.custom.SimpleAlertDialog
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.util.wrapper.stringResource

@Preview
@Composable
fun AccountScreenPreview() {
    AccountScreen(AccountScreenUiState())
}

@Composable
fun AccountScreen(
    accountScreenUiState: AccountScreenUiState,
    showUpButton: Boolean = true,
    onUpPressed: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onEncKeyChange: (String) -> Unit = {},
    onClickCreateUpdate: () -> Unit = {},
    onClickDialogBtnOk: () -> Unit = {},
) {
    Surface {
        Scaffold(
            topBar = { TopAppBarWithUpButton(
                title = stringResource(R.string.screen_title_account),
                onUpPressed = onUpPressed,
                showUpButton = showUpButton
            )},
            bottomBar = {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LocalDimens.current.contentPaddingHorizontal,
                            vertical = LocalDimens.current.itemPaddingVertical
                        ),
                    enabled = accountScreenUiState.isCreateUpdateBtnEnabled(),
                    onClick = onClickCreateUpdate
                ) {
                    if (accountScreenUiState.showProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = stringResource(
                                when (accountScreenUiState.mode) {
                                    Mode.Create -> R.string.button_label_create_account
                                    Mode.Update -> R.string.button_label_update
                                }
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(LocalDimens.current.contentPaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(LocalDimens.current.itemPaddingVertical)
            ) {
                // Do not show Name during account creation to simplify
                if (accountScreenUiState.mode == Mode.Update) {
                    RelayOutlinedTextField(
                        value = accountScreenUiState.name ?: "",
                        labelRes = R.string.text_field_label_name,
                        enabled = accountScreenUiState.isNameTextFieldEnabled,
                        onValueChange = onNameChange,
                        // Similar to inputType=textPersonName
                        capitalization = KeyboardCapitalization.Words
                    )
                }
                RelayOutlinedTextField(
                    value = accountScreenUiState.phone ?: "",
                    labelRes = R.string.text_field_label_phone,
                    enabled = accountScreenUiState.isPhoneTextFieldEnabled,
                    errorMessage = getString(accountScreenUiState.errorStrIdForPhoneField),
                    onValueChange = onPhoneChange,
                    keyboardType = KeyboardType.Phone
                )
                RelayOutlinedTextField(
                    value = accountScreenUiState.encKey ?: "",
                    labelRes = R.string.text_field_label_enc_key,
                    enabled = accountScreenUiState.isEncKeyTextFieldEnabled,
                    onValueChange = onEncKeyChange,
                    supportingText = stringResource(R.string.enc_key_supporting_text),
                    errorMessage = stringResource(accountScreenUiState.errorStrResForEncKeyField)
                )

                if (accountScreenUiState.showDialog) {
                    SimpleAlertDialog(
                        getString(
                            resId = accountScreenUiState.errorStrIdForGenericError
                        ) ?: "",
                        onClickDialogBtnOk
                    )
                }
            }

        }
    }
}

/**
 * Convenient fun that returns String corresponding to [resId]
 */
@Composable
fun getString(resId: Int?): String? {
    return if (resId != null && resId > 0) {
        stringResource(id = resId)
    } else {
        null
    }
}