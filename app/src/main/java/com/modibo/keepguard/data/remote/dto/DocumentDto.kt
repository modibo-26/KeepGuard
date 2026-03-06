package com.modibo.keepguard.data.remote.dto

data class DocumentDto(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val fileUrl: String = "",
    val thumbnailUrl: String = "",
    val mimeType: String = "",
    val fileSize: Long = 0,
    val ocrText: String? = null,
    val scannedAt: Long = 0,
    val createdAt: Long = 0,
)