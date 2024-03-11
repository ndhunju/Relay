package com.ndhunju.barcode.ui.scanner

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.common.Barcode
import com.ndhunju.barcode.R
import com.ndhunju.barcode.barcodedetection.BarcodeFrameProcessor
import com.ndhunju.barcode.barcodedetection.FailureBarcodeGraphic
import com.ndhunju.barcode.barcodedetection.LoadingBarcodeGraphic
import com.ndhunju.barcode.barcodedetection.SuccessBarcodeGraphic
import com.ndhunju.barcode.camera.CameraSource
import com.ndhunju.barcode.camera.GraphicOverlay
import com.ndhunju.barcode.camera.WorkflowModel
import com.ndhunju.barcode.camera.WorkflowModel.WorkflowState
import com.ndhunju.barcode.isPermissionGranted
import com.ndhunju.barcode.requestPermission
import java.io.IOException

/**
 * Screen for barcode scanning using camera preview.
 */
abstract class BarcodeScanningActivity : FragmentActivity() {

    // Variables
    var graphicOverlay: GraphicOverlay? = null
    private set
    lateinit var workflowModel: WorkflowModel
    private set

    var uiState: UiState
        get() { return workflowModel.uiStateFlow.value }
        set(value) { workflowModel.updateUiState(value) }

    private var currentWorkflowState: WorkflowState = WorkflowState.NOT_STARTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workflowModel = viewModels<WorkflowModel>().value

        setContent {
            BarcodeScanningScreen(
                uiState = workflowModel.uiStateFlow.collectAsStateWithLifecycle(),
                onGraphicLayerInitialized = { view -> onGraphicLayerInitialized(view)},
                onClickGraphicOverlay = { /* Do nothing for now*/ },
                onClickCloseIcon = { onBackPressedDispatcher.onBackPressed() },
                onClickFlashIcon = { workflowModel.uiStateFlow.value.toggleFlash() }
            )
        }
    }

    private fun onGraphicLayerInitialized(graphicOverlay: GraphicOverlay) {
        this.graphicOverlay = graphicOverlay
        setUpWorkflowModel(graphicOverlay)
        workflowModel.setWorkflowState(WorkflowState.DETECTING)
    }

    override fun onResume() {
        super.onResume()
        setupIfPermissionsGranted()
    }

    private fun setupIfPermissionsGranted() {

        if (isPermissionGranted(Manifest.permission.CAMERA).not()) {
            requestPermission(REQUEST_CAMERA_PERMISSION, Manifest.permission.CAMERA)
            return
        }

        workflowModel.markCameraFrozen()
        currentWorkflowState = WorkflowState.NOT_STARTED
    }


    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        workflowModel.releaseCameraSource()
        workflowModel.cameraSource = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupIfPermissionsGranted()
            } else {
                finish()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun startCameraPreview(graphicOverlay: GraphicOverlay) {
        if (workflowModel.isCameraLive.not()) {
            try {
                graphicOverlay.clear()
                workflowModel.cameraSource = (CameraSource(graphicOverlay).apply {
                    setFrameProcessor(BarcodeFrameProcessor(graphicOverlay, workflowModel))
                })
                workflowModel.isCameraLive = true
                uiState.setPromptText(null)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start camera preview!", e)
                workflowModel.cameraSource?.release()
                workflowModel.cameraSource = null
                workflowModel.isCameraLive = false
            }
        }

    }

    private fun stopCameraPreview() {

        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            workflowModel.isFlashOn = false
            workflowModel.cameraSource = null
        }

    }

    private fun setUpWorkflowModel(graphicOverlay: GraphicOverlay) {

        // Observes the workflow state changes, if happens,
        // update the overlay view indicators and camera preview state.
        workflowModel.workflowState.observe(this, Observer { workflowState ->
            if (workflowState == null || currentWorkflowState == workflowState) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Log.d(TAG, "Current workflow state: ${currentWorkflowState.name}")

            when (workflowState) {
                WorkflowState.DETECTING -> { startCameraPreview(graphicOverlay) }
                // It becomes CONFIRMING when Barcode is not centered
                WorkflowState.CONFIRMING -> {
                    workflowModel.uiStateFlow.value
                        .setPromptText(getString(R.string.barcode_prompt_move_camera_closer))
                    startCameraPreview(graphicOverlay)
                }
                WorkflowState.DETECTED -> { stopCameraPreview() }
                else -> {
                    workflowModel.uiStateFlow.value
                        .setPromptText(getString(R.string.activity_barcode_workflow_unknown_state))
                }
            }

        })

        workflowModel.detectedBarcode.observe(this) { barcode ->
            onBarcodeDetected(barcode)
        }
    }

    private val loadingAnimator: ValueAnimator by lazy {
        ValueAnimator.ofFloat(0f, 1f).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            duration = 2000
            addUpdateListener {
                graphicOverlay?.invalidate()
            }
        }
    }

    protected fun applyLoadingGraphics() {
        loadingAnimator.start()
        graphicOverlay?.apply {
            clear()
            add(LoadingBarcodeGraphic(this, loadingAnimator))
        }
    }

    protected fun applySuccessGraphics() {
        graphicOverlay?.apply {
            clear()
            add(SuccessBarcodeGraphic(this))
        }
    }

    protected fun applyFailureGraphics() {
        graphicOverlay?.apply {
            clear()
            add(FailureBarcodeGraphic(this))
        }
    }

    private fun onBarcodeDetected(barcode: Barcode) {
        barcode.rawValue?.let { onBarcodeRawValue(it) }
    }

    protected open fun onBarcodeRawValue(rawValue: String) {
        // Show barcode's value on a snack bar
        workflowModel.setSnackBarVisuals(object : SnackbarVisuals {
            override val actionLabel: String?
                get() = null
            override val duration: SnackbarDuration
                get() = SnackbarDuration.Short
            override val message: String
                get() = rawValue
            override val withDismissAction: Boolean
                get() = true

        })
    }

    companion object {
        private val TAG = BarcodeScanningActivity::class.simpleName
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}
