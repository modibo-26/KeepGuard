package com.modibo.keepguard.presentation.screen.warranty.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import com.modibo.keepguard.domain.usecase.warranty.AddWarrantyUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantyByIdUseCase
import com.modibo.keepguard.domain.usecase.warranty.UpdateWarrantyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class WarrantyFormState(
    val type: WarrantyType = WarrantyType.MANUFACTURER,
    val startDate: Long? = null,
    val durationMonths: String = "24",
    val provider: String = "",
    val conditions: String = "",
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WarrantyFormViewModel @Inject constructor(
    private val addWarrantyUseCase: AddWarrantyUseCase,
    private val updateWarrantyUseCase: UpdateWarrantyUseCase,
    private val getWarrantyByIdUseCase: GetWarrantyByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var assetId: String = savedStateHandle.get<String>("assetId") ?: ""
    private val warrantyId: String = savedStateHandle.get<String>("warrantyId") ?: ""

    private val _state = MutableStateFlow(WarrantyFormState())
    val state: StateFlow<WarrantyFormState> = _state

    init {
        if (warrantyId.isNotEmpty()) {
            loadWarranty()
        }
    }

    fun onTypeChange(type: WarrantyType) { _state.value = _state.value.copy(type = type) }
    fun onStartDateChange(date: Long) { _state.value = _state.value.copy(startDate = date) }
    fun onDurationChange(value: String) { _state.value = _state.value.copy(durationMonths = value) }
    fun onProviderChange(value: String) { _state.value = _state.value.copy(provider = value) }
    fun onConditionsChange(value: String) { _state.value = _state.value.copy(conditions = value) }

    private fun calculateEndDate(startDate: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }

    fun saveWarranty() {
        val userId = getCurrentUserUseCase()?.id ?: return
        val startDate = _state.value.startDate ?: return
        val months = _state.value.durationMonths.toIntOrNull() ?: return
        val endDate = calculateEndDate(startDate, months)

        val warranty = Warranty(
            id = warrantyId,
            assetId = assetId,
            userId = userId,
            type = _state.value.type,
            startDate = startDate,
            durationMonths = months,
            endDate = endDate,
            provider = _state.value.provider,
            conditions = _state.value.conditions,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val flow = if (warrantyId.isNotEmpty()) updateWarrantyUseCase(warranty) else addWarrantyUseCase(warranty)
            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isSaved = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    private fun loadWarranty() {
        viewModelScope.launch {
            getWarrantyByIdUseCase(warrantyId).collect { resource ->
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
