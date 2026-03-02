package com.modibo.keepguard.domain.usecase.maintenance

import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.repository.MaintenanceRepository
import javax.inject.Inject

class AddMaintenanceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(maintenance: Maintenance) = repository.addMaintenance(maintenance)
}