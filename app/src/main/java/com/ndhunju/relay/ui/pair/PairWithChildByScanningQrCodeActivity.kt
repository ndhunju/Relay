package com.ndhunju.relay.ui.pair

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ndhunju.barcode.ui.scanner.BarcodeScannerActivity
import com.ndhunju.barcode.camera.WorkflowModel.WorkflowState
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.api.UserNotFoundException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

@Deprecated("Allowing pairing from parent client involves lot of edge cases")
class PairWithChildByScanningQrCodeActivity: BarcodeScannerActivity() {

    private val viewModel: PairWithQrCodeViewModel by viewModels { RelayViewModelFactory }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setUiForInitialState()
    }

    /**
     *  Renders UI to the state where app is actively searching for barcode
     */
    private fun setUiForInitialState() {
        uiState.apply {
            setTitleText(getString(R.string.screen_add_child_enc_key_title))
            setDescriptionText(getString(R.string.screen_add_child_enc_key_scanning_description))
            setFlashOn(false)
        }
    }

    /**
     *  Renders UI to indicate that app is processing the detected barcode
     */
    private fun setUiForScanning() {
        applyLoadingGraphics()
        uiState.setPromptText(getString(R.string.screen_add_child_enc_key_scanning_prompt_scanning))
    }

    /**
     *  Renders UI to indicate that app has finished processing the given barcode.
     */
    @UiThread
    private fun setUiForScanComplete() {
        applySuccessGraphics()
        uiState.setPromptText(getString(R.string.screen_add_child_enc_key_scanning_prompt_scanning_complete))
    }

    /**
     *  Renders UI to indicate that barcode is invalid for our app.
     */
    @UiThread
    private fun setUiForInvalidQrCode(
        errorMsg: String? = null,
    ) {
        applyFailureGraphics()
        uiState.setPromptText(errorMsg ?: getString(R.string.screen_add_child_enc_key_invalid_qr_code))
    }

    private fun showError(message: String) {
        applyFailureGraphics()
        uiState.setPromptText(message)
    }

    override fun onBarcodeRawValue(rawValue: String) {
        super.onBarcodeRawValue(rawValue)
        startPairingWithChildUser(rawValue)
    }


    /**
     * Starts pairing process with the child user's QR code
     */
    private fun startPairingWithChildUser(barcode: String) {
        lifecycleScope.launch {
            viewModel.startPairingWithChildUser(barcode).collect { result ->
                when (result) {
                    is Result.Failure -> {
                        when (result.throwable) {
                            is InvalidParameterException -> setUiForInvalidQrCode()
                            is FirebaseFirestoreException -> {
                                showError(getString(R.string.default_firebase_ex_msg))
                            }
                            is UserNotFoundException -> {
                                showError(getString(R.string.pair_screen_user_email_not_found))
                            }
                            else -> showError(getString(R.string.general_error_message))
                        }
                        // Delay for 3 secs before updating the UI so that
                        // user has enough time to see the error message
                        delay(3000)
                        // Set state back to DETECTING where user can continue with new barcode
                        workflowModel.setWorkflowState(WorkflowState.DETECTING)
                    }

                    is Result.Pending -> {
                        workflowModel.setWorkflowState(WorkflowState.DETECTED)
                        setUiForScanning()
                    }
                    is Result.Success -> {
                        workflowModel.setWorkflowState(WorkflowState.DETECTED)
                        setUiForScanComplete()
                        delay(3000)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

}