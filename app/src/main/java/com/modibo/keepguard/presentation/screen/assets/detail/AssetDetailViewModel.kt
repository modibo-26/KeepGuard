package com.modibo.keepguard.presentation.screen.assets.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.usecase.asset.DeleteAssetUseCase
import com.modibo.keepguard.domain.usecase.asset.GetAssetByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetDetailState(
    val asset: Asset? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
)

@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    private val getAssetByIdUseCase: GetAssetByIdUseCase,
    private val deleteAssetUseCase: DeleteAssetUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val _state = MutableStateFlow(AssetDetailState())
    val state: StateFlow<AssetDetailState> = _state




    init {
        loadAsset()
    }

    fun loadAsset() {
        viewModelScope.launch {
            getAssetByIdUseCase(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(asset = resource.data, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    fun deleteAsset() {
        viewModelScope.launch {
            deleteAssetUseCase(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isDeleted = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }
}