package com.modibo.keepguard.presentation.screen.assets.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.usecase.asset.AddAssetUseCase
import com.modibo.keepguard.domain.usecase.asset.GetAssetByIdUseCase
import com.modibo.keepguard.domain.usecase.asset.UpdateAssetUseCase
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetFormState(
    val name: String = "",
    val description: String = "",
    val category: AssetCategory = AssetCategory.OTHER,
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val purchasePlace: String = "",
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AssetFormViewModel @Inject constructor(
    private val addAssetUseCase: AddAssetUseCase,
    private val getAssetByIdUseCase: GetAssetByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(AssetFormState())
    val state: StateFlow<AssetFormState> = _state

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value) }
    fun onDescriptionChange(value: String) { _state.value = _state.value.copy(description = value) }
    fun onCategoryChange(category: AssetCategory) { _state.value = _state.value.copy(category = category) }
    fun onBrandChange(value: String) { _state.value = _state.value.copy(brand = value) }
    fun onModelChange(value: String) { _state.value = _state.value.copy(model = value) }
    fun onSerialNumberChange(value: String) { _state.value = _state.value.copy(serialNumber = value) }
    fun onPurchasePlaceChange(value: String) { _state.value = _state.value.copy(purchasePlace = value) }

    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    init {
        if (assetId.isNotEmpty()) {
            loadAsset()
        }
    }

    fun saveAsset() {
        val userId = getCurrentUserUseCase()?.id ?: return
        val asset = Asset(
            id = assetId,
            userId = userId,
            name = _state.value.name,
            description = _state.value.description,
            category = _state.value.category,
            brand = _state.value.brand,
            model = _state.value.model,
            serialNumber = _state.value.serialNumber,
            purchasePlace = _state.value.purchasePlace,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val flow = if (assetId.isNotEmpty()) updateAssetUseCase(asset) else addAssetUseCase(asset)
            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isSaved = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }
    fun loadAsset() {
        viewModelScope.launch {
            getAssetByIdUseCase(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val asset = resource.data!!
                        _state.value = _state.value.copy(
                            name = asset.name,
                            description = asset.description,
                            category = asset.category,
                            brand = asset.brand,
                            model = asset.model,
                            serialNumber = asset.serialNumber,
                            purchasePlace = asset.purchasePlace,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }
}
