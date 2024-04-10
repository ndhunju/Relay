package com.ndhunju.relay.ui.pair

import android.os.Bundle
import androidx.activity.viewModels
import com.ndhunju.barcodegeneratorlibrary.BarcodeGeneratorActivity
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.util.wrapper.getString

class ShareEncryptionKeyWithQrCodeActivity : BarcodeGeneratorActivity() {

    val viewModel by viewModels<ShareEncryptionKeyWithQrCodeViewModel> { RelayViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setQrCodeContent(viewModel.getEncryptionKeyInfo())
        setBodyText(getString(viewModel.getBodyText()) ?: "")
    }

}