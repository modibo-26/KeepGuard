package com.modibo.keepguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.remote.dto.AssetDto
import com.modibo.keepguard.data.remote.mapper.toDomain
import com.modibo.keepguard.data.remote.mapper.toDto
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AssetRepository{
    override fun getAssets(): Flow<Resource<List<Asset>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            val snapshot = firestore.collection("assets")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val assets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AssetDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(assets))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun getAssetById(assetId: String): Flow<Resource<Asset>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("assets")
                .document(assetId)
                .get()
                .await()
            val asset = doc
                .toObject(AssetDto::class.java)
                ?.toDomain(doc.id)
            if (asset != null) {
                emit(Resource.Success(asset))
            } else {
                emit(Resource.Error("Asset introuvable"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de fetch"))
        }
    }

    override fun addAsset(asset: Asset): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            firestore.collection("assets")
                .add(asset.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'ajout de l'asset"))
        }
    }

    override fun updateAsset(asset: Asset): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("assets")
                .document(asset.id)
                .set(asset.toDto())
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur dans la modification"))
        }
    }

    override fun deleteAsset(assetId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("assets")
                .document(assetId)
                .delete()
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur dans la suppression"))
        }
    }
}