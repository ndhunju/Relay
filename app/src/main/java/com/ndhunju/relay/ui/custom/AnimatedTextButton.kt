package com.ndhunju.relay.ui.custom

import android.content.res.Configuration
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.extensions.animateBorder


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview // Comment one of the Preview if animation isn't working
@Composable
fun BroaderPreview() {
    RelayTheme {
        Surface {
            AnimatedTextButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = LocalDimens.current.contentPaddingHorizontal,
                        vertical = LocalDimens.current.itemPaddingVertical
                    )
            ) {
                Text(text = "Button")
            }
        }
    }
}

@Composable
fun AnimatedTextButton(
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit
) = TextButton(
    onClick = onClick,
    modifier = modifier
        .animateBorder(
            borderColors = listOf(
                Color.Red,
                Color.Magenta,
                Color.Blue,
                Color.Cyan,
                Color.Green,
                Color.Yellow,
                Color.Red
            ),
            backgroundColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(24.dp),
            borderWidth = 3.dp
    ),
    content = content
)
