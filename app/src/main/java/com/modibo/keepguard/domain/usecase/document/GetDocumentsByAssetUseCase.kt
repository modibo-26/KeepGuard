package com.modibo.keepguard.domain.usecase.document

import com.modibo.keepguard.domain.repository.DocumentRepository
import javax.inject.Inject

class GetDocumentsByAssetUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(assetId: String) = repository.getDocumentsByAsset(assetId)
}