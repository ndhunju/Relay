package com.ndhunju.relay.ui.pair

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.ProgressButton
import com.ndhunju.relay.ui.custom.RelayOutlinedTextField
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Preview
@Composable
fun PairWithParentScreenPreview() {
    PairWithParentScreen()
}
@Composable
fun PairWithParentScreen(
    parentEmailAddress: StateFlow<String> = MutableStateFlow(""),
    onParentEmailAddressChanged: (String) -> Unit = {},
    showProgress: StateFlow<Boolean> = MutableStateFlow(false),
    onClickPair: () -> Unit = {},
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
                    value = parentEmailAddress.collectAsState().value,
                    labelRes = R.string.pair_screen_parent_email_address,
                    onValueChange = onParentEmailAddressChanged,
                    supportingText = stringResource(
                        R.string.pair_screen_parent_email_address_supporting_text
                    )
                )

                ProgressButton(
                    onClick = onClickPair,
                    labelStrRes = R.string.pair_screen_pair,
                    showSpinner = showProgress.collectAsState()
                )
            }
        }
    }
}