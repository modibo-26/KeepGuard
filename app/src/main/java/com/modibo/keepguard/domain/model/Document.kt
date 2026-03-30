package com.modibo.keepguard.domain.model

data class Document(
    val id: String = "",
    val assetId: String = "",
    val warrantyId: String = "",
    val maintenanceId: String = "",
    val userId: String = "",
    val name: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val fileUrl: String = "",
    val thumbnailUrl: String = "",
    val mimeType: String = "",
    val fileSize: Long = 0,
    val date: String = "",
    val amount: String = "",
    val merchant: String = "",
    val ocrText: String? = null,
    val scannedAt: Long = 0,
    val createdAt: Long = 0
)
