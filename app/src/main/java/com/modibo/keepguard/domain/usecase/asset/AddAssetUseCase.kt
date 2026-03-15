package com.modibo.keepguard.domain.usecase.asset

import android.net.Uri
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.repository.AssetRepository
import javax.inject.Inject

class AddAssetUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    operator fun invoke(asset: Asset, imageUri: Uri? = null) = repository.addAsset(asset, imageUri)
}