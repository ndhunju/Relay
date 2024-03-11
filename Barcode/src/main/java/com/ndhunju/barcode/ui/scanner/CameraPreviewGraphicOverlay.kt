package com.ndhunju.barcode.ui.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ndhunju.barcode.camera.GraphicOverlay
import kotlinx.coroutines.flow.StateFlow

@Preview
@Composable
fun CameraPreviewGraphicOverlay(
    promptText: StateFlow<String?>? = null,
    onGraphicLayerInitialized: ((GraphicOverlay) -> Unit)? = null,
    onClickGraphicOverlay: (() -> Unit)? = null,
    onClickCloseIcon: (() -> Unit)? = null,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            ConstraintLayout {
                val (graphicOverlayRef, guideline, promptTextRef) = createRefs()

                AndroidView(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .constrainAs(graphicOverlayRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                            height = Dimension.fillToConstraints
                        }
                        .clickable { onClickGraphicOverlay?.invoke() },
                    factory = { context ->
                        val graphicOverlay = GraphicOverlay(context)
                        onGraphicLayerInitialized?.invoke(graphicOverlay)
                        return@AndroidView graphicOverlay
                    },
                    update = {
                        it.postInvalidate()
                    }
                )

                Spacer(modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .constrainAs(guideline) {
                        bottom.linkTo(parent.bottom)
                    }
                )

                AnimatedVisibility(
                    visible = promptText?.collectAsState()?.value.isNullOrEmpty().not(),
                    enter = expandIn(),
                    exit = shrinkOut()
                    ) {

                }
                Text(
                    text = "Scanning...",
                    modifier = Modifier
                        .wrapContentSize(align = Alignment.Center)
                        .constrainAs(promptTextRef) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(guideline.top)
                            width = Dimension.fillToConstraints
                        },
                    fontSize = 14.sp,
                )
            }
        }
    }
}