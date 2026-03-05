package com.modibo.keepguard.presentation.screen.document.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.MaintenanceType
import com.modibo.keepguard.domain.usecase.document.GetDocumentsByAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentListState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val getDocumentsByAsset: GetDocumentsByAssetUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val _state = MutableStateFlow(DocumentListState())
    val state: StateFlow<DocumentListState> = _state

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        if (assetId.isEmpty()) return
        viewModelScope.launch {
            getDocumentsByAsset(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        documents = resource.data ?: emptyList(),
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