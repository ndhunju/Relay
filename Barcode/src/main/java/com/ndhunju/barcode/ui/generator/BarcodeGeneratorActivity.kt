package com.ndhunju.barcode.ui.generator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity

class BarcodeGeneratorActivity: FragmentActivity() {

    private lateinit var barcodeGeneratorViewModel: BarcodeGeneratorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barcodeGeneratorViewModel = viewModels<BarcodeGeneratorViewModel>().value
        barcodeGeneratorViewModel.processIntent(intent)

        setContent {
            BarcodeGeneratorScreen(
                bitmap = barcodeGeneratorViewModel.qrCodeBitmap
            )
        }
    }
}