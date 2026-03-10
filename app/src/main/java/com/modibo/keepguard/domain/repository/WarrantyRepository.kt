package com.modibo.keepguard.domain.repository

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Warranty
import kotlinx.coroutines.flow.Flow

interface WarrantyRepository {
    fun addWarranty(warranty: Warranty): Flow<Resource<Warranty>>
    fun getWarrantiesByAsset(assetId: String): Flow<Resource<List<Warranty>>>
    fun getWarrantyById(warrantyId: String): Flow<Resource<Warranty>>
    fun getWarrantiesByUser() : Flow<Resource<List<Warranty>>>
    fun updateWarranty(warranty: Warranty): Flow<Resource<Warranty>>
    fun deleteWarranty(warrantyId: String): Flow<Resource<Unit>>
}