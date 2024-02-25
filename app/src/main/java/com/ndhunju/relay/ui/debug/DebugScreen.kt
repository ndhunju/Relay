package com.ndhunju.relay.ui.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.ui.custom.TopAppBarWithUpButton
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme


@Preview
@Composable
fun DebugScreenPreview() {
    DebugScreen()
}
@Composable
fun DebugScreen(
    onClickForceCrash: (() -> Unit)? = null,
    onClickLogs: (() -> Unit)? = null,
    onUpPressed: (() -> Unit)? = null
) {
    RelayTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                topBar = { TopAppBarWithUpButton(
                    title = "Debug Menu",
                    onUpPressed = { onUpPressed?.invoke() }
                )}
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickForceCrash?.invoke() }
                            .padding(
                                vertical = LocalDimens.current.itemPaddingVertical,
                                horizontal = LocalDimens.current.contentPaddingHorizontal
                            ),
                        text = "Force Crash"
                    )

                    Divider()

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickLogs?.invoke() }
                            .padding(
                                vertical = LocalDimens.current.itemPaddingVertical,
                                horizontal = LocalDimens.current.contentPaddingHorizontal
                            ),
                        text = "Logs"
                    )
                }

            }
        }
    }
}