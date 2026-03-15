package com.modibo.keepguard.presentation.screen.document.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.DocumentType
import com.modibo.keepguard.domain.usecase.document.AddDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase

data class DocumentFormState(
    val fileUri: Uri? = null,
    val assetId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val name: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val assets: List<Asset> = emptyList(),
    val fromScanner: Boolean = false
)

@HiltViewModel
class DocumentFormViewModel @Inject constructor(
    private val addDocument: AddDocumentUseCase,
    private val getAssets: GetAssetsUseCase,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""
    private val imageUri: String = savedStateHandle.get<String>("imageUri") ?: ""
    private val ocrText: String = savedStateHandle.get<String>("ocrText") ?: ""

    private val uri = Uri.decode(imageUri)
    private val ocr = Uri.decode(ocrText)

    private val _state = MutableStateFlow(DocumentFormState())
    val state: StateFlow<DocumentFormState> = _state

    init {
        _state.value = _state.value.copy(
            fileUri = if (uri.isNotEmpty()) uri.toUri() else null,
            fromScanner = uri.isNotEmpty(),
            assetId = assetId,
        )
        if (assetId.isEmpty()) {
            loadAssets()
        }
    }

    fun onFileSelected(uri: Uri) { _state.value = _state.value.copy(fileUri = uri) }
    fun onNameChange(name: String) {_state.value = _state.value.copy(name = name)}
    fun onTypeChange(type: DocumentType) { _state.value = _state.value.copy(type = type) }
    fun onAssetSelected(assetId: String) {_state.value = _state.value.copy(assetId = assetId)}

    fun saveDocument() {
        val document = Document(
            assetId = state.value.assetId,
            name = state.value.name,
            type = state.value.type
        )
        viewModelScope.launch {
            val fileUri = state.value.fileUri ?: return@launch
            addDocument(document, fileUri).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isSaved = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    fun loadAssets() {
        viewModelScope.launch {
            getAssets().collect { resource ->
                if (resource is Resource.Success) {
                    _state.value = _state.value.copy(assets = resource.data ?: emptyList())
                }
            }
        }
    }
}