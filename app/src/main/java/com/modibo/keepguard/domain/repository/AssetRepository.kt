package com.modibo.keepguard.domain.repository

import android.net.Uri
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAssets(): Flow<Resource<List<Asset>>>
    fun getAssetById(assetId: String): Flow<Resource<Asset>>
    fun addAsset(asset: Asset,  imageUri: Uri? = null): Flow<Resource<Asset>>
    fun updateAsset(asset: Asset, imageUri: Uri? = null): Flow<Resource<Asset>>
    fun deleteAsset(assetId: String): Flow<Resource<Unit>>
}