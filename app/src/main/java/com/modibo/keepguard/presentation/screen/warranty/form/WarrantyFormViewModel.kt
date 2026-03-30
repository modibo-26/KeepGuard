package com.modibo.keepguard.presentation.screen.warranty.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Constants
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.worker.ReminderScheduler
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
import com.modibo.keepguard.domain.usecase.warranty.AddWarrantyUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantyByIdUseCase
import com.modibo.keepguard.domain.usecase.warranty.UpdateWarrantyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class WarrantyFormStep {
    SOURCE, INFO, RECAP
}

data class WarrantyFormState(
    val step: WarrantyFormStep = WarrantyFormStep.SOURCE,
    val type: WarrantyType = WarrantyType.MANUFACTURER,
    val startDate: Long? = null,
    val durationMonths: String = "24",
    val provider: String = "",
    val conditions: String = "",
    val scannedDocumentUri: Uri? = null,
    val fromScanner: Boolean = false,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WarrantyFormViewModel @Inject constructor(
    private val addWarranty: AddWarrantyUseCase,
    private val updateWarranty: UpdateWarrantyUseCase,
    private val getWarrantyById: GetWarrantyByIdUseCase,
    private val parseDocument: ParseDocumentUseCase,
    private val scheduler: ReminderScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var assetId: String = savedStateHandle.get<String>("assetId") ?: ""
    private val warrantyId: String = savedStateHandle.get<String>("warrantyId") ?: ""

    private val _state = MutableStateFlow(WarrantyFormState())
    val state: StateFlow<WarrantyFormState> = _state

    init {
        if (warrantyId.isNotEmpty()) {
            _state.value = _state.value.copy(isEditing = true, step = WarrantyFormStep.INFO)
            loadWarranty()
        }
    }

    fun onTypeChange(type: WarrantyType) { _state.value = _state.value.copy(type = type) }
    fun onStartDateChange(date: Long) { _state.value = _state.value.copy(startDate = date) }
    fun onDurationChange(value: String) { _state.value = _state.value.copy(durationMonths = value) }
    fun onProviderChange(value: String) { _state.value = _state.value.copy(provider = value) }
    fun onConditionsChange(value: String) { _state.value = _state.value.copy(conditions = value) }

    fun onScanResult(uri: Uri) {
        viewModelScope.launch {
            parseDocument(uri).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val data = resource.data ?: ScannedData()
                        _state.value = _state.value.copy(
                            scannedDocumentUri = uri,
                            fromScanner = true,
                            type = data.warrantyType,
                            durationMonths = if (data.durationMonths > 0) data.durationMonths.toString() else _state.value.durationMonths,
                            provider = data.warrantyProvider.ifEmpty { _state.value.provider },
                            conditions = data.conditions.ifEmpty { _state.value.conditions },
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
        when (state.value.step) {
            WarrantyFormStep.SOURCE -> _state.value = _state.value.copy(step = WarrantyFormStep.INFO)
            WarrantyFormStep.INFO -> _state.value = _state.value.copy(step = WarrantyFormStep.RECAP)
            else -> return
        }
    }

    fun prevStep() {
        when (state.value.step) {
            WarrantyFormStep.INFO -> _state.value = _state.value.copy(
                step = if (state.value.isEditing) WarrantyFormStep.INFO else WarrantyFormStep.SOURCE
            )
            WarrantyFormStep.RECAP -> _state.value = _state.value.copy(step = WarrantyFormStep.INFO)
            else -> return
        }
    }

    private fun calculateEndDate(startDate: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }

    fun saveWarranty() {
        val startDate = _state.value.startDate ?: return
        val months = _state.value.durationMonths.toIntOrNull() ?: return
        val endDate = calculateEndDate(startDate, months)

        val warranty = Warranty(
            id = warrantyId,
            assetId = assetId,
            type = _state.value.type,
            startDate = startDate,
            durationMonths = months,
            endDate = endDate,
            provider = _state.value.provider,
            conditions = _state.value.conditions,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val flow = if (warrantyId.isNotEmpty()) updateWarranty(warranty) else addWarranty(warranty)
            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val savedWarranty = resource.data ?: return@collect
                        scheduler.schedule(
                            savedWarranty.id,
                            "Garantie Expirante",
                            "Votre garantie expire bientot",
                            endDate - Constants.REMINDER_OFFSET_MILLIS
                        )

                        _state.value = _state.value.copy(isSaved = true, isLoading = false)
                    }
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    private fun loadWarranty() {
        viewModelScope.launch {
            getWarrantyById(warrantyId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val warranty = resource.data!!
                        assetId = warranty.assetId
                        _state.value = _state.value.copy(
                            type = warranty.type,
                            startDate = warranty.startDate,
                            durationMonths = warranty.durationMonths.toString(),
                            provider = warranty.provider,
                            conditions = warranty.conditions,
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
}
