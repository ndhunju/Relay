package com.ndhunju.barcode

import android.graphics.*
import android.util.Log
import com.ndhunju.barcode.camera.FrameMetadata
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

interface InputInfo {
    fun getBitmap(): Bitmap?
}

class CameraInputInfo(
    private val frameByteBuffer: ByteBuffer,
    private val frameMetadata: FrameMetadata
) : InputInfo {

    private var bitmap: Bitmap? = null

    @Synchronized
    override fun getBitmap(): Bitmap? {
        return bitmap ?: let {
            bitmap = convertToBitmap(
                frameByteBuffer, frameMetadata.width, frameMetadata.height, frameMetadata.rotation
            )
            bitmap
        }
    }

    /**
     *  Convert NV21 format byte buffer to bitmap.
     */
    private fun convertToBitmap(
        data: ByteBuffer,
        width: Int,
        height: Int,
        rotationDegrees: Int
    ): Bitmap? {

        data.rewind()
        val imageInBuffer = ByteArray(data.limit())
        data.get(imageInBuffer, 0, imageInBuffer.size)
        try {
            val image = YuvImage(
                imageInBuffer, InputImage.IMAGE_FORMAT_NV21, width, height, null
            )
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, width, height), 80, stream)
            val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()

            // Rotate the image back to straight.
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        } catch (e: java.lang.Exception) {
            Log.e("CameraInputInfo", "Error: " + e.message)
        }

        return null
    }
}
