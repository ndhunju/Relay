package com.ndhunju.relay.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.AnimatedTextButton
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.ui.theme.setStatusBarColor

@Preview
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenPreview() {
    RelayTheme {
        WelcomeScreen()
    }
}

@Composable
fun WelcomeScreen(
    onClickNext: () -> Unit = {}
) {
    // Set suitable color to the status bar which is
    // same as surface color with a tint of primary color
    setStatusBarColor(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            .compositeOver(MaterialTheme.colorScheme.surface.copy())
    )

    Surface {
        Scaffold(
            bottomBar = {
                AnimatedTextButton(
                    onClick = onClickNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LocalDimens.current.contentPaddingHorizontal,
                            vertical = LocalDimens.current.itemPaddingVertical
                        )
                ) {
                    Text(text = stringResource(R.string.welcome_screen_create_account_btn))
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(
                        horizontal = LocalDimens.current.contentPaddingHorizontal,
                        vertical = LocalDimens.current.itemPaddingVertical
                    )
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(
                        id = R.string.image_description_app_logo
                    ),
                    modifier = Modifier.size(LocalDimens.current.welcomeLauncherIconSize)
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.025f))
                Text(
                    text = stringResource(
                        R.string.welcome_screen_header,
                        stringResource(id = R.string.app_name)
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = LocalDimens.current.welcomeHeaderTextSize
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                Text(
                    text = stringResource(
                        R.string.welcome_screen_body,
                        stringResource(id = R.string.app_name)
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = LocalDimens.current.welcomeBodyTextSize,
                    color = MaterialTheme.typography.titleMedium.color.copy(
                        alpha = if (isSystemInDarkTheme()) 0F else 0.6F
                    )
                )
            }
        }
    }
}