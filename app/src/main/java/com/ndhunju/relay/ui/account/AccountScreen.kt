package com.ndhunju.relay.ui.account

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens

@Preview
@Composable
fun AccountScreenPreview() {
    AccountScreen(AccountScreenUiState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    accountScreenUiState: AccountScreenUiState,
    onUpPressed: () -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onClickCreateUpdate: () -> Unit = {},
    onClickDialogBtnOk: () -> Unit = {},
) {
    Surface {
        Scaffold(
            topBar = { TopAppBarWithUpButton(
                title = stringResource(R.string.screen_title_account),
                onUpPressed = onUpPressed
            )}
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(LocalDimens.current.contentPaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(LocalDimens.current.itemPaddingVertical)
            ) {
                RelayOutlinedTextField(
                    value = accountScreenUiState.email ?: "",
                    labelRes = R.string.text_field_label_email,
                    enabled = accountScreenUiState.isEmailTextFieldEnabled,
                    errorMessage = getString(accountScreenUiState.errorStrIdForEmailField),
                    onValueChange = onEmailChange
                )
                RelayOutlinedTextField(
                    value = accountScreenUiState.name ?: "",
                    labelRes = R.string.text_field_label_name,
                    enabled = accountScreenUiState.isNameTextFieldEnabled,
                    errorMessage = getString(accountScreenUiState.errorStrIdForNameField),
                    onValueChange = onNameChange
                )
                RelayOutlinedTextField(
                    value = accountScreenUiState.phone ?: "",
                    labelRes = R.string.text_field_label_phone,
                    enabled = accountScreenUiState.isPhoneTextFieldEnabled,
                    errorMessage = getString(accountScreenUiState.errorStrIdForPhoneField),
                    onValueChange = onPhoneChange,
                    keyboardType = KeyboardType.Phone
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = accountScreenUiState.isCreateUpdateBtnEnabled,
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

                if (accountScreenUiState.showDialog) {
                    AlertDialog(onDismissRequest = {}) {
                        Surface {
                            Column(modifier = Modifier
                                .padding(
                                    horizontal = LocalDimens.current.contentPaddingHorizontal,
                                    vertical = LocalDimens.current.itemPaddingVertical
                                ),
                                verticalArrangement = Arrangement.spacedBy(
                                    LocalDimens.current.itemPaddingVertical
                                )
                            ) {
                                Text(
                                    text = getString(
                                        resId = accountScreenUiState.errorStrIdForGenericError
                                    ) ?: ""
                                )
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = onClickDialogBtnOk
                                ) {
                                    Text(text = stringResource(id = R.string.ok))
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun RelayOutlinedTextField(
    value: String,
    onValueChange: ((String) -> Unit) = {},
    @StringRes labelRes: Int,
    enabled: Boolean = true,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
    ) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        isError = errorMessage != null,
        supportingText = {
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = keyboardType),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
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