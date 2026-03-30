package com.modibo.keepguard.data.repository

import android.net.Uri
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.scanner.GeminiService
import com.modibo.keepguard.data.scanner.OcrService
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.repository.ScannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ScannerRepositoryImpl @Inject constructor(
    private val gemini: GeminiService,
    private val ocr: OcrService
) : ScannerRepository {
    override fun parseDocument(imageUri: Uri): Flow<Resource<ScannedData>> = flow {
        emit(Resource.Loading())
        try {
            val text = ocr.extractText(imageUri)
            val scanned = gemini.parseText(text)
            emit(Resource.Success(scanned))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de scan"))
        }
    }
}