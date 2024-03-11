package com.ndhunju.barcode.barcodedetection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.ndhunju.barcode.camera.GraphicOverlay
import com.ndhunju.barcode.camera.GraphicOverlay.Graphic
import com.ndhunju.barcode.R
import com.ndhunju.barcode.settings.Settings

abstract class BarcodeGraphicBase(overlay: GraphicOverlay) : Graphic(overlay) {

    private val defaultStrokeWidth = context.resources.getDimensionPixelOffset(
        R.dimen.barcode_reticle_stroke_width
    ).toFloat()

    protected open var boxBorderPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_stroke)
        style = Style.STROKE
        strokeWidth = defaultStrokeWidth
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_background)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = defaultStrokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    val boxCornerRadius: Float = context.resources.getDimensionPixelOffset(
        R.dimen.barcode_reticle_corner_radius
    ).toFloat()

    val pathPaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Style.STROKE
        strokeWidth = defaultStrokeWidth
        pathEffect = CornerPathEffect(boxCornerRadius)
    }

    val boxRect: RectF = Settings.getBarcodeReticleBox(overlay)

    override fun draw(canvas: Canvas) {
        // Draws the dark background scrim and leaves the box area clear.
        canvas.drawRect(
            0f,
            0f,
            canvas.width.toFloat(),
            canvas.height.toFloat(),
            scrimPaint
        )

        // As the stroke is always centered, so erase twice with FILL and STROKE to clear
        // all area that the box rect would occupy.
        eraserPaint.style = Style.FILL
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        eraserPaint.style = Style.STROKE
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        // Draws the box.
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxBorderPaint)
    }
}
