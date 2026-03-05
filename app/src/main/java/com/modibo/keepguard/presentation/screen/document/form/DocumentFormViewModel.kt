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

data class DocumentFormState(
    val fileUri: Uri? = null,
    val assetId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val name: String = "",
    val type: DocumentType = DocumentType.OTHER
)

@HiltViewModel
class DocumentFormViewModel @Inject constructor(
    private val addDocument: AddDocumentUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val _state = MutableStateFlow(DocumentFormState())
    val state: StateFlow<DocumentFormState> = _state

    fun onFileSelected(uri: Uri) { _state.value = _state.value.copy(fileUri = uri) }
    fun onNameChange(name: String) {_state.value = _state.value.copy(name = name)}
    fun onTypeChange(type: DocumentType) { _state.value = _state.value.copy(type = type) }

    fun saveDocument() {
        val document = Document(
            assetId = assetId,
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

}