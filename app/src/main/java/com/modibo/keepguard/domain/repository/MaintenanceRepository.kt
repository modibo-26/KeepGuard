package com.modibo.keepguard.domain.repository

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Maintenance
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun addMaintenance(maintenance: Maintenance): Flow<Resource<Unit>>
    fun getMaintenancesByAsset(assetId: String): Flow<Resource<List<Maintenance>>>
    fun getMaintenanceById(maintenanceId: String): Flow<Resource<Maintenance>>
    fun getMaintenancesByUser(userId: String) : Flow<Resource<List<Maintenance>>>
    fun updateMaintenance(maintenance: Maintenance): Flow<Resource<Unit>>
    fun deleteMaintenance(maintenanceId: String): Flow<Resource<Unit>>
}