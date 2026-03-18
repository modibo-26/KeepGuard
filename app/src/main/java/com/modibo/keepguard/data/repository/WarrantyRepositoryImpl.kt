package com.modibo.keepguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.modibo.keepguard.core.util.Constants.Collections
import com.modibo.keepguard.core.util.Constants.ErrorMessages
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
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : WarrantyRepository {
    private val entity = "garantie"

    override fun addWarranty(warranty: Warranty): Flow<Resource<Warranty>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val docRef = firestore.collection(Collections.WARRANTIES)
                .add(warranty.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(warranty.copy(id = docRef.id)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.addError(entity)))
        }
    }

    override fun getWarrantiesByAsset(assetId: String): Flow<Resource<List<Warranty>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection(Collections.WARRANTIES)
                .whereEqualTo("assetId", assetId)
                .get()
                .await()
            val warranties = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WarrantyDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(warranties))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun getWarrantyById(warrantyId: String): Flow<Resource<Warranty>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection(Collections.WARRANTIES)
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
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun getWarrantiesByUser(): Flow<Resource<List<Warranty>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val snapshot = firestore.collection(Collections.WARRANTIES)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val warranties = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WarrantyDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(warranties))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun updateWarranty(warranty: Warranty): Flow<Resource<Warranty>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            firestore.collection(Collections.WARRANTIES)
                .document(warranty.id)
                .set(warranty.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(warranty))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.updateError(entity)))
        }
    }

    override fun deleteWarranty(warrantyId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection(Collections.WARRANTIES)
                .document(warrantyId)
                .delete()
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.deleteError(entity)))
        }
    }

}