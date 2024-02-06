package com.ndhunju.relay.ui.custom

import android.content.res.Configuration
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.ui.theme.RelayTheme


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview // Comment one of the Preview if animation isn't working
@Composable
fun BroaderPreview() {
    RelayTheme {
        Surface {
            AnimatedTextButton(
                onClick = { /*TODO*/ },
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
                Color("#ff7534".toColorInt()), // Orange
                Color.Green,
                Color("#369bf7".toColorInt()) // Sky blue
            ),
            backgroundColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(24.dp),
            borderWidth = 3.dp
    ),
    content = content
)
fun Modifier.animateBorder(
    borderColors: List<Color>,
    backgroundColor: Color,
    shape: Shape = RectangleShape,
    borderWidth: Dp = 1.dp,
    animationDurationInMillis: Int = 1000,
    easing: Easing = LinearEasing
): Modifier = composed {
    val brush = Brush.linearGradient(borderColors)
    val infiniteTransition = rememberInfiniteTransition(label = "animateBorder")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationInMillis, easing = easing),
            repeatMode = RepeatMode.Restart
        ), label = "animationAngle"
    )

    this
        .clip(shape)
        .padding(borderWidth)
        .drawWithContent {
            rotate(angle) {
                drawCircle(
                    brush = brush,
                    radius = size.width,
                    blendMode = BlendMode.SrcIn,
                )
            }
            drawContent()
        }
        .background(color = backgroundColor, shape = shape)
}