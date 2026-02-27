package com.modibo.keepguard.presentation.screen.assets.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetListState(
    val assets: List<Asset> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AssetListViewModel @Inject constructor(
    private val getAssetsUseCase: GetAssetsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase

) : ViewModel() {
    private val _state = MutableStateFlow(AssetListState())
    val state: StateFlow<AssetListState> = _state

    init {
        loadAssets()
    }

     fun loadAssets() {
        val userId = getCurrentUserUseCase()?.id
        if(userId != null) {
            viewModelScope.launch {
                getAssetsUseCase(userId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                        is Resource.Success -> _state.value = _state.value.copy(assets = resource.data ?: emptyList(), isLoading = false)
                        is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                    }
                }
            }
        }
    }
}