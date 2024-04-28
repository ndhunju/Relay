package com.ndhunju.relay.ui.pair

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.account.getString
import com.ndhunju.relay.ui.custom.ProgressButton
import com.ndhunju.relay.ui.custom.RelayOutlinedTextField
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.mockChildUsers
import com.ndhunju.relay.ui.theme.LocalDimens
import kotlinx.coroutines.flow.MutableStateFlow

@Preview
@Composable
fun PairWithParentScreenPreview() {
    PairWithParentScreen(
        pairedUserPhoneList = MutableStateFlow(mockChildUsers.map { it.phone }).collectAsState(),
        selectedParentPhoneAddress = MutableStateFlow("+15512345678").collectAsState(),
        isSelectedParentPaired = MutableStateFlow(true).collectAsState()
    )
}
@Composable
fun PairWithParentScreen(
    pairedUserPhoneList: State<List<String>>,
    selectedParentPhoneAddress: State<String>,
    isSelectedParentPaired: State<Boolean>,
    showProgress: State<Boolean>? = null,
    errorMsgResId: State<Int?>? = null,
    onUpPressed: () -> Unit = {},
    onClickPairUnPair: () -> Unit = {},
    onParentPhoneChanged: (String) -> Unit = {},
    onClickPairedUser: (String) -> Unit = {}
) {
    //LogCompositions(tag = "PairWithParentScreen", msg = "Called")
    //Log.d("TAG", "PairWithParentScreen: called")
    Surface {
        Scaffold(
            topBar = {
                TopAppBarWithUpButton(
                    title = stringResource(R.string.screen_title_pair),
                    onUpPressed = onUpPressed
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .padding(
                        vertical = LocalDimens.current.itemPaddingVertical,
                        horizontal = LocalDimens.current.contentPaddingHorizontal
                    )
            ) {
                RelayOutlinedTextField(
                    value = selectedParentPhoneAddress.value,
                    labelRes = R.string.pair_screen_parent_phone_number,
                    onValueChange = onParentPhoneChanged,
                    errorMessage = getString(resId = errorMsgResId?.value),
                    supportingText = stringResource(
                        R.string.pair_screen_parent_phone_number_supporting_text
                    )
                )

                ProgressButton(
                    onClick = onClickPairUnPair,
                    labelStrRes = if (isSelectedParentPaired.value) {
                        R.string.pair_screen_un_pair
                    } else {
                        R.string.pair_screen_pair
                    },
                    showSpinner = showProgress
                )

                Divider()

                Text(
                    text = stringResource(R.string.pair_screen_paired_users),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = LocalDimens.current.itemPaddingVertical)
                        // In Compose, you indicate that a composable is a
                        // heading by defining its semantics property:
                        .semantics { heading() }
                    )

                for (phoneNumber in pairedUserPhoneList.value) {
                    Text(
                        text = phoneNumber,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickPairedUser(phoneNumber) }
                            .padding(vertical = LocalDimens.current.itemPaddingVertical)
                    )
                }
            }
        }
    }
}

@Composable
fun PairWithParentScreen(
    viewModel: PairWithParentViewModel,
    onUpPressed: () -> Unit = {},
) {
    PairWithParentScreen(
        viewModel.pairedUserPhoneList.collectAsState(),
        viewModel.selectedParentPhoneAddress.collectAsState(),
        viewModel.isSelectedParentPaired.collectAsState(),
        viewModel.showProgress.collectAsState(),
        viewModel.errorMsgResId.collectAsState(),
        onUpPressed = onUpPressed,
        onClickPairUnPair = viewModel::onClickPairUnpair,
        onClickPairedUser = viewModel::onClickPairedUser,
        onParentPhoneChanged = viewModel::onSelectedParentPhoneChanged
    )
}