package com.ndhunju.relay.ui.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.RelayOutlinedTextField
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens

/**
 * Fragment that holds the view for letting user pair this app with a parent user.
 * Once paired with a parent user, app will start sending new messages to that parent user.
 */
class PairWithParentFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                PairWithParentScreen()
            }
        }
    }
}

@Composable
fun PairWithParentScreen(
    onParentEmailAddressChanged: (String) -> Unit = {},
    onClickPair: () -> Unit = {},
    onUpPressed: () -> Unit = {}
) {
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
                    value = "",
                    labelRes = R.string.pair_screen_parent_email_address,
                    onValueChange = onParentEmailAddressChanged,
                    supportingText = stringResource(
                        R.string.pair_screen_parent_email_address_supporting_text
                    )
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = LocalDimens.current.itemPaddingVertical),
                    onClick = onClickPair
                ) {
                    Text(text = stringResource(R.string.pair_screen_pair))
                }
            }
        }
    }
}

@Preview
@Composable
fun PairWithParentScreenPreview() {
    PairWithParentScreen()
}