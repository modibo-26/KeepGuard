package com.modibo.keepguard.presentation.screen.scanner

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


data class ScannerState(
    val capturedImageUri: Uri? = null,
    val ocrText: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
): ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state

    fun onImageCaptured(uri: Uri) {
        _state.value = _state.value.copy(isProcessing = true, capturedImageUri = uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(context, uri)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                _state.value = _state.value.copy(ocrText = result.text, isProcessing = false)
            }
            .addOnFailureListener { e ->
                _state.value = _state.value.copy(error = e.message, isProcessing = false)
            }
    }

}