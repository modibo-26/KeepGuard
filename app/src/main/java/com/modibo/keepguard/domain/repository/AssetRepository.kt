package com.modibo.keepguard.domain.repository

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAssets(userId: String): Flow<Resource<List<Asset>>>
    fun getAssetById(assetId: String): Flow<Resource<Asset>>
    fun addAsset(asset: Asset): Flow<Resource<Unit>>
    fun updateAsset(asset: Asset): Flow<Resource<Unit>>
    fun deleteAsset(assetId: String): Flow<Resource<Unit>>
}