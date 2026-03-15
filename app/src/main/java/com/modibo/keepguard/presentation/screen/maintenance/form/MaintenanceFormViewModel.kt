package com.modibo.keepguard.presentation.screen.maintenance.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Constants
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.worker.ReminderScheduler
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.model.MaintenanceType
import com.modibo.keepguard.domain.usecase.maintenance.AddMaintenanceUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByIdUseCase
import com.modibo.keepguard.domain.usecase.maintenance.UpdateMaintenanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceFormState(
    val title: String = "",
    val description: String = "",
    val type: MaintenanceType = MaintenanceType.ONE_TIME,
    val date: Long? = null,
    val cost: String = "",
    val provider: String = "",
    val mileage: String = "",
    val isCompleted: Boolean = false,
    val recurrenceMonths: String = "",
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MaintenanceFormViewModel @Inject constructor(
    private val addMaintenance: AddMaintenanceUseCase,
    private val updateMaintenance: UpdateMaintenanceUseCase,
    private val getMaintenanceById: GetMaintenanceByIdUseCase,
    private val scheduler: ReminderScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var assetId: String = savedStateHandle.get<String>("assetId") ?: ""
    private val maintenanceId: String = savedStateHandle.get<String>("maintenanceId") ?: ""

    private val _state = MutableStateFlow(MaintenanceFormState())
    val state: StateFlow<MaintenanceFormState> = _state

    init {
        if (maintenanceId.isNotEmpty()) {
            loadMaintenance()
        }
    }

    fun onTitleChange(value: String) { _state.value = _state.value.copy(title = value) }
    fun onDescriptionChange(value: String) { _state.value = _state.value.copy(description = value) }
    fun onTypeChange(type: MaintenanceType) { _state.value = _state.value.copy(type = type) }
    fun onDateChange(date: Long) { _state.value = _state.value.copy(date = date) }
    fun onCostChange(value: String) { _state.value = _state.value.copy(cost = value) }
    fun onProviderChange(value: String) { _state.value = _state.value.copy(provider = value) }
    fun onMileageChange(value: String) { _state.value = _state.value.copy(mileage = value) }
    fun onCompletedChange(value: Boolean) { _state.value = _state.value.copy(isCompleted = value) }
    fun onRecurrenceChange(value: String) { _state.value = _state.value.copy(recurrenceMonths = value) }

    fun saveMaintenance() {
        val date = _state.value.date ?: return

        val maintenance = Maintenance(
            id = maintenanceId,
            assetId = assetId,
            title = _state.value.title,
            description = _state.value.description,
            type = _state.value.type,
            date = date,
            cost = _state.value.cost.toDoubleOrNull(),
            provider = _state.value.provider,
            mileage = _state.value.mileage.toIntOrNull(),
            isCompleted = _state.value.isCompleted,
            recurrenceMonths = _state.value.recurrenceMonths.toIntOrNull(),
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val flow = if (maintenanceId.isNotEmpty()) updateMaintenance(maintenance) else addMaintenance(maintenance)
            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isSaved = true, isLoading = false)
                        scheduler.schedule(
                            resource.data!!.id,
                            "Maintenance à faire bientot",
                            "La date de votre entretien approche !",
                            date - Constants.REMINDER_OFFSET_MILLIS
                        )
                    }
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    private fun loadMaintenance() {
        viewModelScope.launch {
            getMaintenanceById(maintenanceId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val m = resource.data!!
                        assetId = m.assetId
                        _state.value = _state.value.copy(
                            title = m.title,
                            description = m.description,
                            type = m.type,
                            date = m.date,
                            cost = m.cost?.toString() ?: "",
                            provider = m.provider,
                            mileage = m.mileage?.toString() ?: "",
                            isCompleted = m.isCompleted,
                            recurrenceMonths = m.recurrenceMonths?.toString() ?: "",
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
