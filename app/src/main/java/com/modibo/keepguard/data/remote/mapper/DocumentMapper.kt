package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.DocumentDto
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.DocumentType

fun DocumentDto.toDomain(id: String) = Document(
    id = id,
    assetId  = assetId,
    userId = userId,
    name = name,
    type =  try {
        DocumentType.valueOf(type)
    } catch (e: Exception) {
        DocumentType.OTHER
    },
    fileUrl = fileUrl,
    thumbnailUrl = thumbnailUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    ocrText = ocrText,
    scannedAt = scannedAt,
    createdAt = createdAt,
)

fun Document.toDto() = DocumentDto(
    assetId  = assetId,
    userId = userId,
    name = name,
    type =  type.name,
    fileUrl = fileUrl,
    thumbnailUrl = thumbnailUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    ocrText = ocrText,
    scannedAt = scannedAt,
    createdAt = createdAt,
)

