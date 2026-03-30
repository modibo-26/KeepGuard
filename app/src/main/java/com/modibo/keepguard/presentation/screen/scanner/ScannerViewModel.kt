package com.modibo.keepguard.presentation.screen.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ScannerState(
    val capturedImageUri: Uri? = null,
    val scanned: ScannedData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val parseDocument: ParseDocumentUseCase,
): ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state

    fun onImageCaptured(uri: Uri) {
        viewModelScope.launch {
            parseDocument(uri).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        scanned = resource.data,
                        capturedImageUri = uri,
                        isLoading = false
                    )
                    is Resource.Error -> _state.value = _state.value.copy(
                        error = resource.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun resetScan() {
        _state.value = _state.value.copy(capturedImageUri = null, scanned = null)
    }

}