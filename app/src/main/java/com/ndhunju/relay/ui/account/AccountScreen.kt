package com.ndhunju.relay.ui.account

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens

@Preview
@Composable
fun AccountScreenPreview() {
    AccountScreen(AccountScreenUiState())
}

@Composable
fun AccountScreen(
    accountScreenUiState: AccountScreenUiState,
    onUpPressed: () -> Unit = {},
    onClickCreateUpdate: () -> Unit = {}
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
                    errorMessage = accountScreenUiState.errorSupportingTextForEmailField
                )
                RelayOutlinedTextField(
                    value = accountScreenUiState.name ?: "",
                    labelRes = R.string.text_field_label_name
                )
                RelayOutlinedTextField(
                    value = accountScreenUiState.phone ?: "",
                    labelRes = R.string.text_field_label_phone,
                    errorMessage = accountScreenUiState.errorSupportingTextForPhone
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClickCreateUpdate
                ) {
                    Text(text = stringResource(
                        when(accountScreenUiState.mode) {
                            Mode.Create -> R.string.button_label_create_account
                            Mode.Update -> R.string.button_label_update
                        }
                    ))
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
    errorMessage: String? = null
    ) {
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
        }
    )
}

/**
 * Data class representing the state of [AccountScreen]
 */
data class AccountScreenUiState(
    val email: String? = null,
    val isEmailTextFieldEnabled: Boolean = true,
    val errorSupportingTextForEmailField: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val errorSupportingTextForPhone: String? = null,
    val mode: Mode = Mode.Create
)

/**
 * All possible modes that user could be using [AccountScreen] in.
 */
sealed class Mode {
    data object Create: Mode()
    data object Update: Mode()
}