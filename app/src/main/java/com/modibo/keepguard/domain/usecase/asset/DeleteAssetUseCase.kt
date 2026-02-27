package com.modibo.keepguard.domain.usecase.asset

import com.modibo.keepguard.domain.repository.AssetRepository
import javax.inject.Inject

class DeleteAssetUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    operator fun invoke(assetId: String) = repository.deleteAsset(assetId)
}