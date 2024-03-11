package com.ndhunju.barcode.ui.generator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

class BarcodeGeneratorViewModel: ViewModel() {

    fun generateQrCode(
        content: String = "",
        sizeInPx: Int = 512
    ): Bitmap {

    }

}