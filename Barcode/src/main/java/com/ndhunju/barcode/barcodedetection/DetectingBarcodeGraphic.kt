package com.ndhunju.barcode.barcodedetection

import android.graphics.Paint
import com.ndhunju.barcode.toColorIntWithOpacity
import com.ndhunju.barcode.R
import com.ndhunju.barcode.camera.GraphicOverlay

class DetectingBarcodeGraphic(overlay: GraphicOverlay) : BarcodeGraphicBase(overlay) {

    override var boxBorderPaint: Paint = Paint().apply {
        color = "#000000".toColorIntWithOpacity(50)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(
            R.dimen.barcode_reticle_stroke_width
        ).toFloat()
    }

}