package com.ndhunju.barcode.ui.generator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Color
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val DEFAULT_QR_CODE_CONTENT = "https://github.com/ndhunju/Relay"
const val BUNDLE_QR_CODE_CONTENT = "QR_CODE_CONTENT"

class BarcodeGeneratorViewModel: ViewModel() {

    private val qrCodeWriter = QRCodeWriter()
    var qrCodeContent = DEFAULT_QR_CODE_CONTENT
        set(value) {
            field = value
            viewModelScope.launch(Dispatchers.IO) {
                _qrCodeBitmap.value = generateQrCode(qrCodeWriter, value)
            }
        }


    //region UI State

    private val _qrCodeBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val qrCodeBitmap: State<Bitmap?> = _qrCodeBitmap
    private val _qrBodyText: MutableState<String?> = mutableStateOf(null)
    val qrBodyText: State<String?> = _qrBodyText

    fun setBodyText(text: String) {
        _qrBodyText.value = text
    }

    //endregion

    fun processIntent(intent: Intent) {
        qrCodeContent = intent.extras?.getString(BUNDLE_QR_CODE_CONTENT) ?: qrCodeContent
    }

    fun generateQrCode(
        qrCodeWriter: QRCodeWriter,
        content: String = DEFAULT_QR_CODE_CONTENT,
        sizeInPx: Int = 512
    ): Bitmap {
        // Make the QR code buffer border narrower
        val hints = hashMapOf<EncodeHintType, Int>().apply { this[EncodeHintType.MARGIN] = 1 }
        val bits = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, sizeInPx, sizeInPx, hints)
        val qrCodeBitmap = Bitmap.createBitmap(sizeInPx, sizeInPx, Config.RGB_565).also { bitmap ->
            for (x in 0 until sizeInPx) {
                for (y in 0 until sizeInPx) {
                    bitmap.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }

        return qrCodeBitmap
    }

}