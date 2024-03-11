package com.ndhunju.barcode.barcodedetection

import android.graphics.Canvas
import android.graphics.Path
import com.ndhunju.barcode.camera.GraphicOverlay
import com.ndhunju.barcode.settings.Settings
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * This class extends [BarcodeGraphicBase] to assist user to move camera closer to the bar code to
 * confirm the detected barcode.
 */
internal class AssistConfirmBarcodeGraphic(
    overlay: GraphicOverlay,
    private val barcode: Barcode
    ) : BarcodeGraphicBase(overlay) {

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw a highlighted path to indicate the current progress to meet size requirement.
        val sizeProgress = Settings.getProgressToMeetBarcodeSizeRequirement(
            overlay,
            barcode
        )

        val path = Path()

        if (sizeProgress > 0.95f) {
            // Have a completed path with all corners rounded.
            path.moveTo(boxRect.left, boxRect.top)
            path.lineTo(boxRect.right, boxRect.top)
            path.lineTo(boxRect.right, boxRect.bottom)
            path.lineTo(boxRect.left, boxRect.bottom)
            path.close()
        } else {
            path.moveTo(boxRect.left, boxRect.top + boxRect.height() * sizeProgress)
            path.lineTo(boxRect.left, boxRect.top)
            path.lineTo(boxRect.left + boxRect.width() * sizeProgress, boxRect.top)

            path.moveTo(boxRect.right, boxRect.bottom - boxRect.height() * sizeProgress)
            path.lineTo(boxRect.right, boxRect.bottom)
            path.lineTo(boxRect.right - boxRect.width() * sizeProgress, boxRect.bottom)
        }

        canvas.drawPath(path, pathPaint)
    }
}
