package com.modibo.keepguard.domain.usecase.document

import com.modibo.keepguard.domain.repository.DocumentRepository
import javax.inject.Inject

class GetDocumentByIdUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(documentId: String) = repository.getDocumentById(documentId)
}