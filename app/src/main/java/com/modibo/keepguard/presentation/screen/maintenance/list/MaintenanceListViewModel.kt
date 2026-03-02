package com.modibo.keepguard.presentation.screen.maintenance.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenancesByAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceListState(
    val maintenances: List<Maintenance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MaintenanceListViewModel @Inject constructor(
    private val getMaintenancesByAssetUseCase: GetMaintenancesByAssetUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val _state = MutableStateFlow(MaintenanceListState())
    val state: StateFlow<MaintenanceListState> = _state

    init {
        loadMaintenances()
    }

    fun loadMaintenances() {
        if (assetId.isEmpty()) return
        viewModelScope.launch {
            getMaintenancesByAssetUseCase(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(
                        maintenances = resource.data ?: emptyList(),
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
