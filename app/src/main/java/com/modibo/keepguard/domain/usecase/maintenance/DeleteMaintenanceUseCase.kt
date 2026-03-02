package com.modibo.keepguard.domain.usecase.maintenance

import com.modibo.keepguard.domain.repository.MaintenanceRepository
import javax.inject.Inject

class DeleteMaintenanceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(maintenanceId: String) = repository.deleteMaintenance(maintenanceId)
}