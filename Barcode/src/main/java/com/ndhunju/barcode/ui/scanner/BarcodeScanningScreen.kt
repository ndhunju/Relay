package com.ndhunju.barcode.ui.scanner

import android.hardware.Camera
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ndhunju.barcode.R
import com.ndhunju.barcode.camera.CameraSource
import com.ndhunju.barcode.camera.CameraSourcePreview
import com.ndhunju.barcode.camera.FrameProcessor
import com.ndhunju.barcode.camera.GraphicOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Preview
@Composable
fun BarcodeScanningScreen(
    uiState: State<UiState> = MutableStateFlow(UiState()).collectAsState(),
    onGraphicLayerInitialized: ((GraphicOverlay) -> Unit)? = null,
    onClickGraphicOverlay: (() -> Unit)? = null,
    onClickCloseIcon: (() -> Unit)? = null,
    onClickFlashIcon: (() -> Unit)? = null,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        BarcodeScanningMainContent(
            Modifier.padding(innerPadding),
            uiState,
            onGraphicLayerInitialized,
            onClickGraphicOverlay,
            onClickCloseIcon,
            onClickFlashIcon
        )

        val snackBarVisuals = uiState.value.snackBarVisuals.collectAsState().value
        if (snackBarVisuals != null) {
            LaunchedEffect(key1 = snackBarHostState) {
                scope.launch {
                    snackBarHostState.showSnackbar(snackBarVisuals)
                }
            }
        }
    }
}

@Composable
fun BarcodeScanningMainContent(
    modifier: Modifier,
    uiState: State<UiState> = MutableStateFlow(UiState()).collectAsState(),
    onGraphicLayerInitialized: ((GraphicOverlay) -> Unit)? = null,
    onClickGraphicOverlay: (() -> Unit)? = null,
    onClickCloseIcon: (() -> Unit)? = null,
    onClickFlashIcon: (() -> Unit)? = null,
) {
    MaterialTheme {
        Surface {
            ConstraintLayout(modifier = modifier.background(Color.Black)) {
                val (topActionBar, title, description) = createRefs()
                KeepScreenOn()

                TopActionBarForCamera(
                    modifier = Modifier.constrainAs(topActionBar) {
                        width = Dimension.matchParent
                        height = Dimension.matchParent
                    },
                    uiState.value.isFlashOn.collectAsState(),
                    onClickCloseIcon,
                    onClickFlashIcon
                ) {
                    CameraSourcePreview(
                        modifier = Modifier.fillMaxSize(),
                        uiState.value.cameraSource.collectAsState(),
                        uiState.value.promptText.collectAsState(),
                        onGraphicLayerInitialized,
                        onClickGraphicOverlay
                    )
                }

                Title(
                    uiState.value.titleText.collectAsState(),
                    Modifier.constrainAs(title) { top.linkTo(parent.top) }
                )

                Description(
                    uiState.value.descriptionText.collectAsState(),
                    Modifier.constrainAs(description) {
                        start.linkTo(parent.start)
                        width = Dimension.fillToConstraints
                    })
            }
        }
    }
}

@Composable
private fun CameraSourcePreview(
    modifier: Modifier,
    cameraSource: State<CameraSource?>,
    promptText: State<String?>,
    onGraphicLayerInitialized: ((GraphicOverlay) -> Unit)?,
    onClickGraphicOverlay: (() -> Unit)? = null,
) {
    AndroidView(
        modifier = modifier.background(Color.Black),
        factory = { context ->
            CameraSourcePreview(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                LayoutInflater.from(context).inflate(
                    R.layout.camera_preview_overlay,
                    this
                )

                val graphicOverlay = findViewById<GraphicOverlay>(
                    R.id.camera_preview_graphic_overlay
                )

                graphicOverlay.setOnClickListener {
                    onClickGraphicOverlay?.invoke()
                }
                onGraphicLayerInitialized?.invoke(graphicOverlay)
            }
        },
        update = { cameraSourcePreview ->
            // Show promptText only if there is a value
            val promptTextView = cameraSourcePreview.findViewById<TextView>(
                R.id.bottom_prompt_text
            )
            if (promptText.value != null) {
                promptTextView.visibility = View.VISIBLE
                promptTextView.text = promptText.value
            } else {
                promptTextView.visibility = View.GONE
            }

            val cameraSourceNonNull = cameraSource.value ?: run {
                cameraSourcePreview.stop()
                return@AndroidView
            }

            cameraSourcePreview.start(cameraSourceNonNull)

        })
}

@Composable
private fun Description(
    descriptionText: State<String?>,
    modifier: Modifier
) {
    Text(
        text = descriptionText.value
            ?: stringResource(id = R.string.activity_barcode_scanning_description),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 154.dp)
            .graphicsLayer {
                translationY = -1.12f
            },
        color = Color.White
    )
}

@Composable
private fun Title(
    titleText: State<String?>,
    modifier: Modifier
) {
    Text(
        modifier = modifier
            .padding(start = 16.dp, top = 110.dp)
            .graphicsLayer { translationY = -0.59f },
        text = titleText.value ?: stringResource(id = R.string.activity_barcode_scanning_title),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(28f, TextUnitType.Sp),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

data class UiState(
    private val _titleText: MutableStateFlow<String?> = MutableStateFlow(null),
    val titleText: StateFlow<String?> = _titleText.asStateFlow(),
    private val _descriptionText: MutableStateFlow<String?> = MutableStateFlow(null),
    val descriptionText: StateFlow<String?> = _descriptionText.asStateFlow(),
    private val _promptText: MutableStateFlow<String?> = MutableStateFlow(null),
    val promptText: StateFlow<String?> = _promptText.asStateFlow(),
    private val _isFlashOn: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow(),
    private val _isCameraLive: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val isCameraLive: StateFlow<Boolean> = _isCameraLive.asStateFlow(),
    private val _cameraSource: MutableStateFlow<CameraSource?> = MutableStateFlow(null),
    val cameraSource: StateFlow<CameraSource?> = _cameraSource.asStateFlow(),
    private val _snackBarVisuals: MutableStateFlow<SnackbarVisuals?> = MutableStateFlow(null),
    val snackBarVisuals: StateFlow<SnackbarVisuals?> = _snackBarVisuals.asStateFlow()
) {

    fun setTitleText(titleText: String) {
        _titleText.value = titleText
    }

    fun setDescriptionText(descriptionText: String?) {
        _descriptionText.value = descriptionText
    }

    fun setPromptText(promptText: String?) {
        _promptText.value = promptText
    }
    fun toggleFlash() {
        setFlashOn(_isFlashOn.value.not())
    }

    fun setFlashOn(flashOn: Boolean) {
        _isFlashOn.value = flashOn
        if (flashOn) {
            _cameraSource.value?.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
        } else {
            _cameraSource.value?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
        }
    }

    fun setIsCameraLive(isCameraLive: Boolean) {
        _isCameraLive.value = isCameraLive
    }

    fun setCameraSource(cameraSource: CameraSource?) {
        _cameraSource.value = cameraSource
    }

    fun setFrameProcessor(processor: FrameProcessor) {
        throw IllegalStateException(
            "Set processor to CameraSource directly before passing it to setCameraSource()"
        )
    }

    fun setSnackBarVisuals(snackBarVisuals: SnackbarVisuals?) {
        _snackBarVisuals.value = snackBarVisuals
    }

}