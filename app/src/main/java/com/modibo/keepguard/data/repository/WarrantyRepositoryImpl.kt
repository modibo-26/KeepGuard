package com.modibo.keepguard.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.remote.dto.WarrantyDto
import com.modibo.keepguard.data.remote.mapper.toDomain
import com.modibo.keepguard.data.remote.mapper.toDto
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.repository.WarrantyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WarrantyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WarrantyRepository {
    override fun addWarranty(warranty: Warranty): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("warranties")
                .add(warranty.toDto())
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'ajout de la garantie"))
        }
    }

    override fun getWarrantiesByAsset(assetId: String): Flow<Resource<List<Warranty>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("warranties")
                .whereEqualTo("assetId", assetId)
                .get()
                .await()
            val warranties = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WarrantyDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(warranties))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun getWarrantyById(warrantyId: String): Flow<Resource<Warranty>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("warranties")
                .document(warrantyId)
                .get()
                .await()
            val warranty = doc
                .toObject(WarrantyDto::class.java)
                ?.toDomain(doc.id)
            if (warranty != null) {
                emit(Resource.Success(warranty))
            } else {
                emit(Resource.Error("Garantie introuvable"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun getWarrantiesByUser(userId: String): Flow<Resource<List<Warranty>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("warranties")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val warranties = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WarrantyDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(warranties))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun updateWarranty(warranty: Warranty): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("warranties")
                .document(warranty.id)
                .set(warranty.toDto())
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur dans la modification"))
        }
    }

    override fun deleteWarranty(warrantyId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("warranties")
                .document(warrantyId)
                .delete()
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur dans la suppression"))
        }
    }

}