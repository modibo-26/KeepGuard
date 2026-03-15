package com.modibo.keepguard.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : AssetRepository{
    override fun getAssets(): Flow<Resource<List<Asset>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            val snapshot = firestore.collection("assets")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    override fun addAsset(
        asset: Asset,
        imageUri: Uri?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val assetId = firestore.collection("assets").document().id
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            val finalAsset = if (imageUri != null) {
                val ref = storage.reference.child("users/${userId}/assets/$assetId/image")
                ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                asset.copy(imageUrl = downloadUrl)
            } else {
                asset
            }
            firestore.collection("assets")
                .document(assetId).set(finalAsset.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'ajout de l'asset"))
        }
    }

    override fun updateAsset(
        asset: Asset,
        imageUri: Uri?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Non connecté")
            val finalAsset = if (imageUri != null) {
                val ref = storage.reference.child("users/${userId}/assets/${asset.id}/image")
                ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                asset.copy(imageUrl = downloadUrl)
            } else {
                asset
            }
            firestore.collection("assets")
                .document(asset.id)
                .set(finalAsset.toDto().copy(userId = userId))
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