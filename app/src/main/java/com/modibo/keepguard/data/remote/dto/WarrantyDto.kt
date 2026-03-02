package com.modibo.keepguard.data.remote.dto

data class WarrantyDto (
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val type: String = "",
    val startDate: Long = 0,
    val durationMonths: Int = 24,
    val endDate: Long = 0,
    val provider: String = "",
    val conditions: String = "",
    val documentIds: List<String> = emptyList(),
    val createdAt: Long = 0
)