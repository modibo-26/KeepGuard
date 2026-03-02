package com.modibo.keepguard.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyStatus
import com.modibo.keepguard.domain.model.status
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByUserUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantiesByUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val assets: List<Asset> = emptyList(),
    val warranties: List<Warranty> = emptyList(),
    val maintenances: List<Maintenance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val assetCount: Int get() = assets.size
    val activeWarranties: Int get() = warranties.count { it.status == WarrantyStatus.ACTIVE }
    val expiringWarranties: List<Warranty> get() = warranties.filter { it.status == WarrantyStatus.EXPIRING_SOON }
    val expiredWarranties: Int get() = warranties.count { it.status == WarrantyStatus.EXPIRED }
    val pendingMaintenances: List<Maintenance> get() = maintenances.filter { !it.isCompleted }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAssetsUseCase: GetAssetsUseCase,
    private val getWarrantiesByUserUseCase: GetWarrantiesByUserUseCase,
    private val getMaintenanceByUserUseCase: GetMaintenanceByUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        loadData()
    }

    fun loadData() {
        val userId = getCurrentUserUseCase()?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            launch {
                getAssetsUseCase(userId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {}
                        is Resource.Success -> _state.value = _state.value.copy(
                            assets = resource.data ?: emptyList()
                        )
                        is Resource.Error -> _state.value = _state.value.copy(error = resource.message)
                    }
                }
            }
            launch {
                getWarrantiesByUserUseCase(userId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {}
                        is Resource.Success -> _state.value = _state.value.copy(
                            warranties = resource.data ?: emptyList()
                        )
                        is Resource.Error -> _state.value = _state.value.copy(error = resource.message)
                    }
                }
            }
            launch {
                getMaintenanceByUserUseCase(userId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {}
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
}
