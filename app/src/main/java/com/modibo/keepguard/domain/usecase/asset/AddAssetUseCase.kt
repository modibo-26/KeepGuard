package com.modibo.keepguard.domain.usecase.asset

import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.repository.AssetRepository
import javax.inject.Inject

class AddAssetUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    operator fun invoke(asset: Asset) = repository.addAsset(asset)
}