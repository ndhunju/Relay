package com.ndhunju.barcode.ui.scanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.barcode.R
import kotlinx.coroutines.flow.MutableStateFlow

@Preview
@Composable
fun TopActionBarForCamera(
    modifier: Modifier = Modifier,
    setFlashOn: State<Boolean> = MutableStateFlow(false).collectAsState(),
    onClickCloseIcon: (() -> Unit)? = null,
    onClickFlashIcon: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    MaterialTheme {
        Surface {
            Box(modifier = modifier.fillMaxSize()) {
                content?.invoke()
                Row(Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(Color.Black, Color.Transparent))
                    )
                    .padding(12.dp), Arrangement.SpaceBetween
                ) {
                    Image(
                        painterResource(R.drawable.ic_close_white_24),
                        stringResource(R.string.content_description_close_button),
                        Modifier.clickable { onClickCloseIcon?.invoke() }
                    )

                    Image(
                        painterResource(
                            if (setFlashOn.value) R.drawable.ic_flash_off_white_24
                            else R.drawable.ic_flash_on_white_24
                        ),
                        stringResource(R.string.content_description_flash_button),
                        Modifier.clickable { onClickFlashIcon?.invoke() }
                    )
                }
            }
        }
    }
}