package com.modibo.keepguard.presentation.screen.maintenance.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceDetailState(
    val maintenance: Maintenance? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class MaintenanceDetailViewModel @Inject constructor(
    private val getMaintenanceById: GetMaintenanceByIdUseCase,
    private val deleteMaintenance: DeleteMaintenanceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val maintenanceId: String = savedStateHandle.get<String>("maintenanceId") ?: ""

    private val _state = MutableStateFlow(MaintenanceDetailState())
    val state: StateFlow<MaintenanceDetailState> = _state

    init {
        loadMaintenance()
    }

    fun loadMaintenance() {
        viewModelScope.launch {
            getMaintenanceById(maintenanceId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        maintenance = resource.data,
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

    fun deleteMaintenance() {
        viewModelScope.launch {
            deleteMaintenance(maintenanceId).collect { resource ->
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
