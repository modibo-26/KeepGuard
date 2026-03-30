package com.modibo.keepguard.domain.usecase.scanner

import android.net.Uri
import com.modibo.keepguard.domain.repository.ScannerRepository
import javax.inject.Inject

class ParseDocumentUseCase @Inject constructor(
    private val repository: ScannerRepository
) {
    operator fun invoke(imageUri: Uri) = repository.parseDocument(imageUri)
}