package com.modibo.keepguard.presentation.screen.document.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.DocumentType
import com.modibo.keepguard.domain.usecase.document.AddDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantiesByUserUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByUserUseCase
import kotlinx.serialization.json.Json

enum class DocumentFormStep {
    SOURCE, INFO, LINK, RECAP
}

data class DocumentFormState(
    val step: DocumentFormStep = DocumentFormStep.SOURCE,
    val fileUri: Uri? = null,
    val assetId: String = "",
    val warrantyId: String = "",
    val maintenanceId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val name: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val date: String = "",
    val amount: String = "",
    val merchant: String = "",
    val assets: List<Asset> = emptyList(),
    val warranties: List<com.modibo.keepguard.domain.model.Warranty> = emptyList(),
    val maintenances: List<com.modibo.keepguard.domain.model.Maintenance> = emptyList(),
    val fromScanner: Boolean = false
)

@HiltViewModel
class DocumentFormViewModel @Inject constructor(
    private val addDocument: AddDocumentUseCase,
    private val getAssets: GetAssetsUseCase,
    private val getWarranties: GetWarrantiesByUserUseCase,
    private val getMaintenances: GetMaintenanceByUserUseCase,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val assetId: String = savedStateHandle.get<String>("assetId") ?: ""
    private val imageUri: String = savedStateHandle.get<String>("imageUri") ?: ""
    private val scannedJson: String = savedStateHandle.get<String>("scannedJson") ?: ""


    private val json = Json { ignoreUnknownKeys = true }
    private val uri = Uri.decode(imageUri)
    private val documentInfo = if (scannedJson.isNotEmpty())
        json.decodeFromString<ScannedData>(Uri.decode(scannedJson))
    else
        ScannedData()

    private val _state = MutableStateFlow(DocumentFormState())
    val state: StateFlow<DocumentFormState> = _state

    init {
        val fromScanner = uri.isNotEmpty()
        _state.value = _state.value.copy(
            fileUri = if (fromScanner) uri.toUri() else null,
            fromScanner = fromScanner,
            step = if (fromScanner) DocumentFormStep.INFO else DocumentFormStep.SOURCE,
            assetId = assetId,
            name = documentInfo.name,
            type = documentInfo.type,
            date = documentInfo.date,
            amount = documentInfo.amount,
            merchant = documentInfo.merchant,
        )
        loadAssets()
        loadWarranties()
        loadMaintenances()
    }

    fun onFileSelected(uri: Uri) { _state.value = _state.value.copy(fileUri = uri) }
    fun onNameChange(name: String) {_state.value = _state.value.copy(name = name)}
    fun onTypeChange(type: DocumentType) { _state.value = _state.value.copy(type = type) }
    fun onAssetSelected(assetId: String) {_state.value = _state.value.copy(assetId = assetId)}
    fun onDateChange(date: String) { _state.value = _state.value.copy(date = date) }
    fun onAmountChange(amount: String) { _state.value = _state.value.copy(amount = amount) }
    fun onMerchantChange(merchant: String) { _state.value = _state.value.copy(merchant = merchant) }
    fun onWarrantySelected(warrantyId: String) { _state.value = _state.value.copy(warrantyId = warrantyId) }
    fun onMaintenanceSelected(maintenanceId: String) { _state.value = _state.value.copy(maintenanceId = maintenanceId) }

    fun nextStep() {
        when (state.value.step) {
            DocumentFormStep.SOURCE -> _state.value = _state.value.copy(step = DocumentFormStep.INFO)
            DocumentFormStep.INFO -> _state.value = _state.value.copy(step = DocumentFormStep.LINK)
            DocumentFormStep.LINK -> _state.value = _state.value.copy(step = DocumentFormStep.RECAP)
            else -> return
        }
    }

    fun prevStep() {
        when (state.value.step) {
            DocumentFormStep.INFO -> _state.value = _state.value.copy(
                step = if (state.value.fromScanner) DocumentFormStep.INFO else DocumentFormStep.SOURCE
            )
            DocumentFormStep.LINK -> _state.value = _state.value.copy(step = DocumentFormStep.INFO)
            DocumentFormStep.RECAP -> _state.value = _state.value.copy(step = DocumentFormStep.LINK)
            else -> return
        }
    }

    fun saveDocument() {
        val s = state.value
        val document = Document(
            assetId = s.assetId,
            warrantyId = s.warrantyId,
            maintenanceId = s.maintenanceId,
            name = s.name,
            type = s.type,
            date = s.date,
            amount = s.amount,
            merchant = s.merchant
        )
        viewModelScope.launch {
            val fileUri = state.value.fileUri ?: return@launch
            addDocument(document, fileUri).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isSaved = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    fun loadAssets() {
        viewModelScope.launch {
            getAssets().collect { resource ->
                if (resource is Resource.Success) {
                    _state.value = _state.value.copy(assets = resource.data ?: emptyList())
                }
            }
        }
    }

    private fun loadWarranties() {
        viewModelScope.launch {
            getWarranties().collect { resource ->
                if (resource is Resource.Success) {
                    _state.value = _state.value.copy(warranties = resource.data ?: emptyList())
                }
            }
        }
    }

    private fun loadMaintenances() {
        viewModelScope.launch {
            getMaintenances().collect { resource ->
                if (resource is Resource.Success) {
                    _state.value = _state.value.copy(maintenances = resource.data ?: emptyList())
                }
            }
        }
    }
}