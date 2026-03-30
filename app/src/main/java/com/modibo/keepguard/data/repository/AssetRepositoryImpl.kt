package com.modibo.keepguard.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.graphics.scale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.modibo.keepguard.core.util.Constants.Collections
import com.modibo.keepguard.core.util.Constants.ErrorMessages
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.remote.dto.AssetDto
import com.modibo.keepguard.data.remote.mapper.toDomain
import com.modibo.keepguard.data.remote.mapper.toDto
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.repository.AssetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @param:ApplicationContext private val context: Context
) : AssetRepository {
    private val entity = "asset"

    override fun getAssets(): Flow<Resource<List<Asset>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val snapshot = firestore.collection(Collections.ASSETS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val assets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AssetDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(assets))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun getAssetById(assetId: String): Flow<Resource<Asset>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection(Collections.ASSETS)
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
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun addAsset(
        asset: Asset,
        imageUri: Uri?
    ): Flow<Resource<Asset>> = flow {
        emit(Resource.Loading())
        try {
            val assetId = firestore.collection(Collections.ASSETS).document().id
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val finalAsset = if (imageUri != null) {
                val ref = storage.reference.child("users/${userId}/assets/$assetId/image")
                val bytes = compressImage(context, imageUri)
                ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                asset.copy(imageUrl = downloadUrl)
            } else {
                asset
            }
            firestore.collection(Collections.ASSETS)
                .document(assetId).set(finalAsset.toDto().copy(userId = userId))
                .await()
            val savedAsset = finalAsset.copy(id = assetId)
            emit(Resource.Success(savedAsset))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.addError(entity)))
        }
    }

    override fun updateAsset(
        asset: Asset,
        imageUri: Uri?
    ): Flow<Resource<Asset>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val finalAsset = if (imageUri != null) {
                val ref = storage.reference.child("users/${userId}/assets/${asset.id}/image")
                val bytes = compressImage(context, imageUri)
                ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                asset.copy(imageUrl = downloadUrl)
            } else {
                asset
            }
            firestore.collection(Collections.ASSETS)
                .document(asset.id)
                .set(finalAsset.toDto().copy(userId = userId))
                .await()
            emit(Resource.Success(finalAsset))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.updateError(entity)))
        }
    }

    override fun deleteAsset(assetId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection(Collections.ASSETS).document(assetId).get().await()
            val imageUrl = doc.getString("imageUrl")
            if (!imageUrl.isNullOrEmpty()) {
                storage.getReferenceFromUrl(imageUrl).delete().await()
            }
            firestore.collection(Collections.ASSETS).document(assetId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.deleteError(entity)))
        }
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw IllegalArgumentException("Cannot open URI")
        }
        val scaled = bitmap.scale(1024, (1024f * bitmap.height / bitmap.width).toInt())
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
        scaled.recycle()
        return output.toByteArray()
    }
}