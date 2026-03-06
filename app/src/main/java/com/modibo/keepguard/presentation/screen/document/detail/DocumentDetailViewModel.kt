package com.modibo.keepguard.presentation.screen.document.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.usecase.document.DeleteDocumentUseCase
import com.modibo.keepguard.domain.usecase.document.GetDocumentByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentDetailState(
    val document: Document? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
)

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val getDocumentById: GetDocumentByIdUseCase,
    private val deleteDocument: DeleteDocumentUseCase,
     savedStateHandle: SavedStateHandle
): ViewModel() {
    private val documentId: String = savedStateHandle.get<String>("documentId") ?: ""

    private val _state = MutableStateFlow(DocumentDetailState())
    val state: StateFlow<DocumentDetailState> = _state

    init {
        loadDocument()
    }

    fun loadDocument() {
        viewModelScope.launch {
            getDocumentById(documentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        document = resource.data,
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

    fun onDeleteDocument() {
        viewModelScope.launch {
            deleteDocument(documentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        isDeleted = true,
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
}