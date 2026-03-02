package com.modibo.keepguard.presentation.screen.warranty.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantiesByAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WarrantyListState(
    val warranties: List<Warranty> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WarrantyListViewModel @Inject constructor(
    private val getWarrantiesByAssetUseCase: GetWarrantiesByAssetUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val _state = MutableStateFlow(WarrantyListState())
    val state: StateFlow<WarrantyListState> = _state

    init {
        loadWarranties()
    }

    fun loadWarranties() {
        if (assetId.isEmpty()) return
        viewModelScope.launch {
            getWarrantiesByAssetUseCase(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        warranties = resource.data ?: emptyList(),
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
