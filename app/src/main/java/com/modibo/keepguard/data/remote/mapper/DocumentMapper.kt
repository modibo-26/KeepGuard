package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.DocumentDto
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.DocumentType

fun DocumentDto.toDomain(id: String) = Document(
    id = id,
    assetId  = assetId,
    warrantyId = warrantyId,
    maintenanceId = maintenanceId,
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
    date = date,
    amount = amount,
    merchant = merchant,
    ocrText = ocrText,
    scannedAt = scannedAt,
    createdAt = createdAt,
)

fun Document.toDto() = DocumentDto(
    assetId  = assetId,
    warrantyId = warrantyId,
    maintenanceId = maintenanceId,
    userId = userId,
    name = name,
    type =  type.name,
    fileUrl = fileUrl,
    thumbnailUrl = thumbnailUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    date = date,
    amount = amount,
    merchant = merchant,
    ocrText = ocrText,
    scannedAt = scannedAt,
    createdAt = createdAt,
)

