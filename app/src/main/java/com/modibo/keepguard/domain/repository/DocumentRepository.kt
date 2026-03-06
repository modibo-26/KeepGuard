package com.modibo.keepguard.domain.repository

import android.net.Uri
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun addDocument(document: Document, fileUri: Uri): Flow<Resource<Unit>>
    fun getDocumentsByAsset(assetId: String): Flow<Resource<List<Document>>>
    fun getDocumentById(documentId: String): Flow<Resource<Document>>
    fun deleteDocument(documentId: String): Flow<Resource<Unit>>
}