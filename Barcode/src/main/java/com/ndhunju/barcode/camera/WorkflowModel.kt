package com.ndhunju.barcode.camera

import androidx.annotation.MainThread
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.ndhunju.barcode.ui.scanner.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * View model for handling application workflow based on camera preview.
 */
class WorkflowModel : ViewModel() {

    val workflowState = MutableLiveData<WorkflowState>()
    val detectedBarcode = MutableLiveData<Barcode>()

    private val _uiStateFlow = MutableStateFlow(UiState())
    val uiStateFlow: StateFlow<UiState> = _uiStateFlow.asStateFlow()

    var cameraSource: CameraSource?
        get() { return _uiStateFlow.value.cameraSource.value }
        set(value) { _uiStateFlow.value.setCameraSource(value) }

    var isCameraLive: Boolean
        get() = _uiStateFlow.value.isCameraLive.value
        set(value) { _uiStateFlow.value.setIsCameraLive(value) }

    var isFlashOn: Boolean
        get() = _uiStateFlow.value.isFlashOn.value
        set(value) { _uiStateFlow.value.setFlashOn(value)
    }

    /**
     * State set of the application workflow.
     */
    enum class WorkflowState {
        NOT_STARTED,
        DETECTING,
        DETECTED,
        CONFIRMING,
    }

    @MainThread
    fun setWorkflowState(workflowState: WorkflowState) {
        this.workflowState.value = workflowState
    }

    fun markCameraFrozen() {
        _uiStateFlow.value.setIsCameraLive(false)
    }

    fun updateUiState(uiState: UiState) {
        _uiStateFlow.value = uiState
    }

    fun releaseCameraSource() {
        _uiStateFlow.value.cameraSource.value?.release()
    }

    fun setSnackBarVisuals(snackBarVisuals: SnackbarVisuals) {
        _uiStateFlow.value.setSnackBarVisuals(snackBarVisuals)

    }

}
