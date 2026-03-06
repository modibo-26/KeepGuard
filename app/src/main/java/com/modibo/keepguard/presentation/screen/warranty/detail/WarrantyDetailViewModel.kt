package com.modibo.keepguard.presentation.screen.warranty.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.usecase.warranty.DeleteWarrantyUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantyByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WarrantyDetailState(
    val warranty: Warranty? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class WarrantyDetailViewModel @Inject constructor(
    private val getWarrantyById: GetWarrantyByIdUseCase,
    private val deleteWarranty: DeleteWarrantyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val warrantyId: String = savedStateHandle.get<String>("warrantyId") ?: ""

    private val _state = MutableStateFlow(WarrantyDetailState())
    val state: StateFlow<WarrantyDetailState> = _state

    init {
        loadWarranty()
    }

    fun loadWarranty() {
        viewModelScope.launch {
            getWarrantyById(warrantyId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        warranty = resource.data,
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

    fun deleteWarranty() {
        viewModelScope.launch {
            deleteWarranty(warrantyId).collect { resource ->
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
