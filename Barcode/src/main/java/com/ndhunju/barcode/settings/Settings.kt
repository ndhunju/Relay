package com.ndhunju.barcode.settings

import android.graphics.RectF
import com.google.android.gms.common.images.Size
import com.google.mlkit.vision.barcode.common.Barcode
import com.ndhunju.barcode.camera.CameraSizePair
import com.ndhunju.barcode.camera.GraphicOverlay

/**
 * Class to store different settings that can be changed.
 * */
object Settings {

    private var previewSize: CameraSizePair? = null
    private var pictureSize: CameraSizePair? = null
    private var enableBarCodeSizeCheck = false
    private var minBarcodeWidth = 50
    private var reticleWidth = 80
    private var reticleHeight = 35

    fun getProgressToMeetBarcodeSizeRequirement(
        overlay: GraphicOverlay,
        barcode: Barcode
    ): Float {
        return if (enableBarCodeSizeCheck) {
            val reticleBoxWidth = getBarcodeReticleBox(overlay).width()
            val barcodeWidth = overlay.translateX(barcode.boundingBox?.width()?.toFloat() ?: 0f)
            val requiredWidth = reticleBoxWidth * minBarcodeWidth / 100
            (barcodeWidth / requiredWidth).coerceAtMost(1f)
        } else {
            1f
        }
    }

    fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * reticleWidth / 100
        val boxHeight = overlayHeight * reticleHeight / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2

        return RectF(
            cx - boxWidth / 2,
            cy - boxHeight / 2,
            cx + boxWidth / 2,
            cy + boxHeight / 2
        )
    }

    fun setPreviewSize(size: Size) {
        previewSize =  CameraSizePair(size, size)
    }

    fun getPreviewSize(): CameraSizePair? {
        return previewSize
    }

    fun setPictureSize(size: Size) {
        pictureSize =  CameraSizePair(size, size)
    }

    fun getPictureSize(): CameraSizePair? {
        return pictureSize
    }

}
