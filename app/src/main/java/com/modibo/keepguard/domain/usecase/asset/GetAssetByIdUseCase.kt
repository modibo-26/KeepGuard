package com.modibo.keepguard.domain.usecase.asset

import com.modibo.keepguard.domain.repository.AssetRepository
import javax.inject.Inject

class GetAssetByIdUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    operator fun invoke(assetId: String) = repository.getAssetById(assetId)
}