package com.ndhunju.relay.ui.pair

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterfaceDummyImpl
import com.ndhunju.relay.service.UserSettingsPersistServiceDummyImpl
import com.ndhunju.relay.ui.account.getString
import com.ndhunju.relay.ui.custom.ProgressButton
import com.ndhunju.relay.ui.custom.RelayOutlinedTextField
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.util.CurrentUser

@Preview
@Composable
fun PairWithParentScreenPreview() {
    PairWithParentScreen(
        PairWithParentViewModel(
            ApiInterfaceDummyImpl(),
            CurrentUser,
            UserSettingsPersistServiceDummyImpl()
        )
    )
}
@Composable
fun PairWithParentScreen(
    viewModel: PairWithParentViewModel,
    onUpPressed: () -> Unit = {}
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
                    value = viewModel.parentEmailAddress.collectAsState().value,
                    labelRes = R.string.pair_screen_parent_email_address,
                    onValueChange = viewModel::onParentEmailAddressChanged,
                    errorMessage = getString(resId = viewModel.errorMsgResId.collectAsState().value),
                    supportingText = stringResource(
                        R.string.pair_screen_parent_email_address_supporting_text
                    )
                )

                ProgressButton(
                    onClick = viewModel::onClickPair,
                    labelStrRes = if (viewModel.isPaired.collectAsState().value) {
                        R.string.pair_screen_un_pair
                    } else {
                        R.string.pair_screen_pair
                    },
                    showSpinner = viewModel.showProgress.collectAsState()
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
                    )

                for (email in viewModel.pairedUserEmailList) {
                    Text(
                        text = email,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = LocalDimens.current.itemPaddingVertical)
                    )
                }
            }
        }
    }
}