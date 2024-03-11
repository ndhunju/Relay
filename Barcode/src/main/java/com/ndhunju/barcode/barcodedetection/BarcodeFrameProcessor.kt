package com.ndhunju.barcode.barcodedetection

import android.graphics.Rect
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.graphics.toRectF
import com.google.android.gms.tasks.Task
import com.ndhunju.barcode.InputInfo
import com.ndhunju.barcode.camera.CameraReticleAnimator
import com.ndhunju.barcode.camera.GraphicOverlay
import com.ndhunju.barcode.camera.WorkflowModel
import com.ndhunju.barcode.camera.WorkflowModel.WorkflowState
import com.ndhunju.barcode.camera.FrameProcessorBase
import com.ndhunju.barcode.settings.Settings
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

/**
 * A processor to run the barcode detector.
 */
class BarcodeFrameProcessor(
    private val graphicOverlay: GraphicOverlay,
    private val workflowModel: WorkflowModel
    ) : FrameProcessorBase<List<Barcode>>() {

    private val scanner = BarcodeScanning.getClient()
    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)

    override fun detectInImage(image: InputImage): Task<List<Barcode>> =
        scanner.process(image)

    @MainThread
    override fun onSuccess(
        inputInfo: InputInfo,
        results: List<Barcode>,
        graphicOverlay: GraphicOverlay
    ) {

        if (workflowModel.uiStateFlow.value.isCameraLive.value.not()) return

        // Picks the barcode, if exists, that covers the center of graphic overlay.

        val barcodeInCenter = results.firstOrNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox)
            val overlayRect = Rect()
            graphicOverlay.getGlobalVisibleRect(overlayRect)
            box.intersect(overlayRect.toRectF())
        }

        graphicOverlay.clear()

        if (barcodeInCenter == null) {
            graphicOverlay.add(DetectingBarcodeGraphic(graphicOverlay))
            cameraReticleAnimator.start()
            graphicOverlay.add(BarcodeReticleGraphic(graphicOverlay, cameraReticleAnimator))
            workflowModel.setWorkflowState(WorkflowState.DETECTING)
        } else {
            cameraReticleAnimator.cancel()

            val sizeProgress = Settings.getProgressToMeetBarcodeSizeRequirement(
                graphicOverlay,
                barcodeInCenter
            )

            if (sizeProgress < 1) {
                // Barcode in the camera view is too small, so prompt user to move camera closer.
                graphicOverlay.add(AssistConfirmBarcodeGraphic(graphicOverlay, barcodeInCenter))
                workflowModel.setWorkflowState(WorkflowState.CONFIRMING)
            } else {
                workflowModel.detectedBarcode.value = barcodeInCenter
                workflowModel.setWorkflowState(WorkflowState.DETECTED)
            }
        }

        graphicOverlay.invalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed!", e)
    }

    override fun stop() {
        super.stop()
        try {
            scanner.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close barcode detector!", e)
        }
    }

    companion object {
        private const val TAG = "BarcodeProcessor"
    }
}
