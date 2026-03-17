package com.modibo.keepguard.presentation.screen.assets.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.model.AssetCondition
import com.modibo.keepguard.domain.model.AssetSubCategory
import com.modibo.keepguard.domain.model.PurchaseMode
import com.modibo.keepguard.domain.usecase.asset.AddAssetUseCase
import com.modibo.keepguard.domain.usecase.asset.GetAssetByIdUseCase
import com.modibo.keepguard.domain.usecase.asset.UpdateAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Month
import javax.inject.Inject

data class AssetFormState(
    val name: String = "",
    val description: String = "",
    val category: AssetCategory = AssetCategory.OTHER,
    val subCategory: AssetSubCategory? = null,
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val purchasePlace: String = "",
    val imageUrl: String = "",
    val imageUri: Uri? = null,
    val purchaseMode: PurchaseMode = PurchaseMode.UNKNOWN,
    val purchaseDate: Long? = null,
    val purchasePrice: Double? = null,
    val originalCreatedAt: Long = 0,
    val assetFormStep: AssetFormStep = AssetFormStep.PHOTO,
    val condition: AssetCondition = AssetCondition.NEW,
    val warrantyMonths: Int? = null,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class AssetFormStep {
    PHOTO, CATEGORY, INFO_PURCHASE, RECAP
}

@HiltViewModel
class AssetFormViewModel @Inject constructor(
    private val addAsset: AddAssetUseCase,
    private val getAssetById: GetAssetByIdUseCase,
    private val updateAsset: UpdateAssetUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(AssetFormState())
    val state: StateFlow<AssetFormState> = _state

    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    init {
        if (assetId.isNotEmpty()) {
            _state.value = _state.value.copy(isEditing = true)
            loadAsset()
        }
    }

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value) }
    fun onDescriptionChange(value: String) { _state.value = _state.value.copy(description = value) }
    fun onCategoryChange(value: AssetCategory) { _state.value = _state.value.copy(category = value) }
    fun onSubCategoryChange(value: AssetSubCategory) { _state.value = _state.value.copy(subCategory = value) }
    fun onPurchaseModeChange(value: PurchaseMode) { _state.value = _state.value.copy(purchaseMode = value) }
    fun onPurchaseDateChange(value: Long?) { _state.value = _state.value.copy(purchaseDate = value) }
    fun onPurchasePriceChange(purchasePricevalue: Double?) { _state.value = _state.value.copy(purchasePrice = purchasePricevalue) }
    fun onBrandChange(value: String) { _state.value = _state.value.copy(brand = value) }
    fun onModelChange(value: String) { _state.value = _state.value.copy(model = value) }
    fun onSerialNumberChange(value: String) { _state.value = _state.value.copy(serialNumber = value) }
    fun onPurchasePlaceChange(value: String) { _state.value = _state.value.copy(purchasePlace = value) }
    fun onImageUriChange(value: Uri?) { _state.value = _state.value.copy(imageUri = value) }
    fun onConditionChange(value: AssetCondition) { _state.value = _state.value.copy(condition = value) }
    fun onWarrantyMonthsChange(value: Int?) { _state.value = _state.value.copy(warrantyMonths = value) }

    fun saveAsset() {
        val s = _state.value
        val asset = Asset(
            id = assetId,
            name = s.name,
            description = s.description,
            category = s.category,
            subCategory = s.subCategory,
            purchaseMode = s.purchaseMode,
            condition = s.condition,
            warrantyMonths = s.warrantyMonths,
            brand = s.brand,
            model = s.model,
            serialNumber = s.serialNumber,
            purchasePlace = s.purchasePlace,
            imageUrl = s.imageUrl,
            purchaseDate = s.purchaseDate,
            purchasePrice = s.purchasePrice,
            createdAt = if (assetId.isNotEmpty()) s.originalCreatedAt else System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val imageUri = state.value.imageUri
            val flow = if (assetId.isNotEmpty()) updateAsset(asset, imageUri) else addAsset(asset,  imageUri)
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
            getAssetById(assetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val asset = resource.data!!
                        _state.value = _state.value.copy(
                            name = asset.name,
                            description = asset.description,
                            category = asset.category,
                            subCategory = asset.subCategory,
                            purchaseMode = asset.purchaseMode,
                            condition = asset.condition,
                            warrantyMonths = asset.warrantyMonths,
                            brand = asset.brand,
                            model = asset.model,
                            serialNumber = asset.serialNumber,
                            purchasePlace = asset.purchasePlace,
                            imageUrl = asset.imageUrl,
                            purchaseDate = asset.purchaseDate,
                            purchasePrice = asset.purchasePrice,
                            originalCreatedAt = asset.createdAt,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }
    fun nextStep() {
        when(state.value.assetFormStep) {
            AssetFormStep.PHOTO -> _state.value = _state.value.copy(assetFormStep = AssetFormStep.CATEGORY)
            AssetFormStep.CATEGORY -> _state.value = _state.value.copy(assetFormStep = AssetFormStep.INFO_PURCHASE)
            AssetFormStep.INFO_PURCHASE -> _state.value = _state.value.copy(assetFormStep = AssetFormStep.RECAP)
            else -> return
        }
    }
    fun prevStep() {
        when(state.value.assetFormStep) {
            AssetFormStep.CATEGORY -> _state.value = _state.value.copy(assetFormStep = AssetFormStep.PHOTO)
            AssetFormStep.INFO_PURCHASE -> _state.value = _state.value.copy(assetFormStep = AssetFormStep.CATEGORY)
            AssetFormStep.RECAP -> _state.value = _state.value.copy(assetFormStep = AssetFormStep.INFO_PURCHASE)
            else -> return
        }
    }
}
