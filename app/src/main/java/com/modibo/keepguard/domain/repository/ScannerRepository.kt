package com.modibo.keepguard.domain.repository

import android.net.Uri
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.ScannedData
import kotlinx.coroutines.flow.Flow

interface ScannerRepository {
    fun parseDocument(imageUri: Uri): Flow<Resource<ScannedData>>
}