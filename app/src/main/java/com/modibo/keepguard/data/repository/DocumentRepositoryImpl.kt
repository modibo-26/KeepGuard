package com.modibo.keepguard.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.modibo.keepguard.core.util.Constants.Collections
import com.modibo.keepguard.core.util.Constants.ErrorMessages
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.remote.dto.DocumentDto
import com.modibo.keepguard.data.remote.mapper.toDomain
import com.modibo.keepguard.data.remote.mapper.toDto
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
): DocumentRepository {
    private val entity = "document"

    override fun addDocument(
        document: Document,
        fileUri: Uri
    ): Flow<Resource<Document>> = flow {
        emit(Resource.Loading())
        try {
            val docId = firestore.collection(Collections.DOCUMENTS).document().id
            val userId = auth.currentUser?.uid ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val ref = storage.reference.child("users/${userId}/documents/$docId/file")
            ref.putFile(fileUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            val updateDoc = document.copy(fileUrl = downloadUrl, userId = userId)
            firestore.collection(Collections.DOCUMENTS)
                .document(docId)
                .set(updateDoc.toDto())
                .await()
            val savedDoc = updateDoc.copy(id = docId)
            emit(Resource.Success(savedDoc))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.addError(entity)))
        }
    }

    override fun getDocumentsByAsset(assetId: String): Flow<Resource<List<Document>>> = flow  {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection(Collections.DOCUMENTS)
                .whereEqualTo("assetId", assetId)
                .get()
                .await()
            val documents = snapshot.documents.mapNotNull { doc ->
                doc.toObject(DocumentDto::class.java)?.toDomain(doc.id)
            }
            emit(Resource.Success(documents))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun getDocumentById(documentId: String): Flow<Resource<Document>> = flow  {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection(Collections.DOCUMENTS)
                .document(documentId)
                .get()
                .await()
            val document = doc
                .toObject(DocumentDto::class.java)
                ?.toDomain(doc.id)
            if (document != null) {
                emit(Resource.Success(document))
            } else {
                emit(Resource.Error("Document introuvable"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.fetchError(entity)))
        }
    }

    override fun deleteDocument(documentId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection(Collections.DOCUMENTS).document(documentId).get().await()
            val fileUrl = doc.getString("fileUrl")
            if (!fileUrl.isNullOrEmpty()) {
                storage.getReferenceFromUrl(fileUrl).delete().await()
            }
            firestore.collection(Collections.DOCUMENTS).document(documentId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.deleteError(entity)))
        }
    }
}