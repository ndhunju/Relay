package com.ndhunju.barcode.barcodedetection

import android.graphics.Paint
import androidx.core.graphics.toColorInt
import com.ndhunju.barcode.R
import com.ndhunju.barcode.camera.GraphicOverlay

class SuccessBarcodeGraphic(overlay: GraphicOverlay) : BarcodeGraphicBase(overlay) {

    override var boxBorderPaint: Paint = Paint().apply {
        color = "#6EDA78".toColorInt()
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(
            R.dimen.barcode_reticle_stroke_width
        ).toFloat()
    }

}