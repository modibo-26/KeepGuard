package com.modibo.keepguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.modibo.keepguard.core.util.Constants.Collections
import com.modibo.keepguard.core.util.Constants.ErrorMessages
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.remote.dto.MaintenanceDto
import com.modibo.keepguard.data.remote.mapper.toDomain
import com.modibo.keepguard.data.remote.mapper.toDto
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
): MaintenanceRepository {
    private val entity = "entretien"

    override fun addMaintenance(maintenance: Maintenance): Flow<Resource<Maintenance>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val docRef = firestore.collection(Collections.MAINTENANCES)
                .add(maintenance.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(maintenance.copy(id = docRef.id)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.addError(entity)))
        }
    }

    override fun getMaintenancesByAsset(assetId: String): Flow<Resource<List<Maintenance>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection(Collections.MAINTENANCES)
                .whereEqualTo("assetId", assetId)
                .get()
                .await()
            val maintenances = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MaintenanceDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(maintenances))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun getMaintenanceById(maintenanceId: String): Flow<Resource<Maintenance>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection(Collections.MAINTENANCES)
                .document(maintenanceId)
                .get()
                .await()
            val maintenance = doc
                .toObject(MaintenanceDto::class.java)
                ?.toDomain(doc.id)
            if (maintenance != null) {
                emit(Resource.Success(maintenance))
            } else {
                emit(Resource.Error("Maintenance introuvable"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun getMaintenancesByUser(): Flow<Resource<List<Maintenance>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val snapshot = firestore.collection(Collections.MAINTENANCES)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val maintenances = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MaintenanceDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(maintenances))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun updateMaintenance(maintenance: Maintenance): Flow<Resource<Maintenance>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            firestore.collection(Collections.MAINTENANCES)
                .document(maintenance.id)
                .set(maintenance.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(maintenance))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.updateError(entity)))
        }
    }

    override fun deleteMaintenance(maintenanceId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection(Collections.MAINTENANCES)
                .document(maintenanceId)
                .delete()
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.deleteError(entity)))
        }
    }
}