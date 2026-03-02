package com.modibo.keepguard.domain.usecase.maintenance

import com.modibo.keepguard.domain.repository.MaintenanceRepository
import javax.inject.Inject

class GetMaintenanceByIdUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(maintenanceId: String) = repository.getMaintenanceById(maintenanceId)
}