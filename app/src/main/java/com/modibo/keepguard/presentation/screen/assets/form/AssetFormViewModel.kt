package com.modibo.keepguard.presentation.screen.assets.form

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.model.AssetCondition
import com.modibo.keepguard.domain.model.AssetSubCategory
import com.modibo.keepguard.domain.model.PurchaseMode
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.usecase.asset.AddAssetUseCase
import com.modibo.keepguard.domain.usecase.asset.GetAssetByIdUseCase
import com.modibo.keepguard.domain.usecase.asset.UpdateAssetUseCase
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
import com.modibo.keepguard.domain.usecase.warranty.AddWarrantyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
    val scannedDocumentUri: Uri? = null,
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
    private val parseDocument: ParseDocumentUseCase,
    private val addWarranty: AddWarrantyUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(AssetFormState())
    val state: StateFlow<AssetFormState> = _state

    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""

    private val imageUri: String = savedStateHandle.get<String>("imageUri") ?: ""
    private val scannedJson: String = savedStateHandle.get<String>("scannedJson") ?: ""

    private val json = Json { ignoreUnknownKeys = true }
    private val uri = Uri.decode(imageUri)
    private val documentInfo = if (scannedJson.isNotEmpty())
        json.decodeFromString<ScannedData>(Uri.decode(scannedJson))
    else
        ScannedData()

    init {
        val fromScanner = uri.isNotEmpty()
        _state.value = _state.value.copy(
            scannedDocumentUri = if (fromScanner) uri.toUri() else null,
            name = documentInfo.name,
            brand = documentInfo.brand,
            model = documentInfo.model,
            serialNumber = documentInfo.serialNumber,
            category = documentInfo.category,
            purchasePlace = documentInfo.purchasePlace,
            purchasePrice = documentInfo.purchasePrice.toDoubleOrNull(),
            warrantyMonths = if (documentInfo.warrantyMonths > 0) documentInfo.warrantyMonths else null,
        )
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
    fun onPurchasePriceChange(purchasePriceValue: Double?) { _state.value = _state.value.copy(purchasePrice = purchasePriceValue) }
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
            val flow = if (assetId.isNotEmpty()) updateAsset(asset, imageUri) else addAsset(asset, imageUri)
            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val savedAsset = resource.data ?: return@collect

                        // Créer les warranties auto (seulement à la création)
                        if (assetId.isEmpty()) {
                            val now = System.currentTimeMillis()
                            createAutoWarranties(savedAsset.id, s, now)
                        }

                        _state.value = _state.value.copy(isSaved = true, isLoading = false)
                    }
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    private suspend fun createAutoWarranties(assetId: String, s: AssetFormState, now: Long) {
        kotlinx.coroutines.coroutineScope {
            // Garantie légale de conformité
            val legalMonths = when (s.condition) {
                AssetCondition.NEW -> 24
                AssetCondition.RECONDITIONED -> 12
                else -> null
            }
            legalMonths?.let {
                launch {
                    addWarranty(
                        Warranty(
                            assetId = assetId,
                            type = WarrantyType.LEGAL,
                            startDate = s.purchaseDate ?: now,
                            durationMonths = it,
                            endDate = (s.purchaseDate ?: now) + (it * 30.44 * 24 * 60 * 60 * 1000).toLong(),
                        )
                    ).collect {}
                }
            }

            // Garantie constructeur
            s.warrantyMonths?.let {
                if (it > 0) {
                    launch {
                        addWarranty(
                            Warranty(
                                assetId = assetId,
                                type = WarrantyType.MANUFACTURER,
                                startDate = s.purchaseDate ?: now,
                                durationMonths = it,
                                endDate = (s.purchaseDate ?: now) + (it * 30.44 * 24 * 60 * 60 * 1000).toLong(),
                            )
                        ).collect {}
                    }
                }
            }

            // Droit de rétractation (achat en ligne)
            if (s.purchaseMode == PurchaseMode.ONLINE) {
                launch {
                    addWarranty(
                        Warranty(
                            assetId = assetId,
                            type = WarrantyType.RETRACTATION,
                            startDate = s.purchaseDate ?: now,
                            durationMonths = 0,
                            endDate = (s.purchaseDate ?: now) + (14 * 24 * 60 * 60 * 1000L),
                        )
                    ).collect {}
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

    fun onImageCaptured(uri: Uri) {
        viewModelScope.launch {
            parseDocument(uri).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val data = resource.data ?: ScannedData()
                        _state.value = _state.value.copy(
                            scannedDocumentUri = uri,
                            brand = data.brand,
                            model = data.model,
                            serialNumber = data.serialNumber,
                            category = data.category,
                            name = data.name,
                            purchasePlace = data.purchasePlace,
                            purchasePrice = data.purchasePrice.toDoubleOrNull(),
                            warrantyMonths = if (data.warrantyMonths > 0) data.warrantyMonths else null,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> _state.value = _state.value.copy(
                        error = resource.message,
                        isLoading = false
                    )
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
