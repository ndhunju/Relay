package com.ndhunju.relay.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.theme.LocalDimens

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}

@Composable
fun WelcomeScreen() {
    Surface {
        Scaffold(
            bottomBar = {
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LocalDimens.current.contentPaddingHorizontal,
                            vertical = LocalDimens.current.itemPaddingVertical
                        ),
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
                    fontSize = LocalDimens.current.welcomeBodyTextSize
                )
            }
        }
    }
}