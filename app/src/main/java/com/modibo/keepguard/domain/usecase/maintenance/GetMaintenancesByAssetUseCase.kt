package com.modibo.keepguard.domain.usecase.maintenance

import com.modibo.keepguard.domain.repository.MaintenanceRepository
import javax.inject.Inject

class GetMaintenancesByAssetUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(assetId: String) = repository.getMaintenancesByAsset(assetId)
}