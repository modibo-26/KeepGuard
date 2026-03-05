package com.modibo.keepguard.domain.usecase.document

import android.net.Uri
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.repository.DocumentRepository
import javax.inject.Inject

class AddDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(document: Document, fileUri: Uri) = repository.addDocument(document, fileUri)
}