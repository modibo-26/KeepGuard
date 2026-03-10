package com.modibo.keepguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    override fun addMaintenance(maintenance: Maintenance): Flow<Resource<Maintenance>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            val docRef = firestore.collection("maintenances")
                .add(maintenance.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(maintenance.copy(id = docRef.id)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'ajout de la maintenance"))
        }
    }

    override fun getMaintenancesByAsset(assetId: String): Flow<Resource<List<Maintenance>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("maintenances")
                .whereEqualTo("assetId", assetId)
                .get()
                .await()
            val maintenances = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MaintenanceDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(maintenances))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun getMaintenanceById(maintenanceId: String): Flow<Resource<Maintenance>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("maintenances")
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
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun getMaintenancesByUser(): Flow<Resource<List<Maintenance>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            val snapshot = firestore.collection("maintenances")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val maintenances = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MaintenanceDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(maintenances))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun updateMaintenance(maintenance: Maintenance): Flow<Resource<Maintenance>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("maintenances")
                .document(maintenance.id)
                .set(maintenance.toDto())
                .await()
            emit(Resource.Success(maintenance))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur dans la modification"))
        }
    }

    override fun deleteMaintenance(maintenanceId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("maintenances")
                .document(maintenanceId)
                .delete()
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur dans la suppression"))
        }
    }
}