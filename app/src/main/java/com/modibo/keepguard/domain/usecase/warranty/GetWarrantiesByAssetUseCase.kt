package com.modibo.keepguard.domain.usecase.warranty

import com.modibo.keepguard.domain.repository.WarrantyRepository
import javax.inject.Inject

class GetWarrantiesByAssetUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    operator fun invoke(assetId: String) = repository.getWarrantiesByAsset(assetId)
}