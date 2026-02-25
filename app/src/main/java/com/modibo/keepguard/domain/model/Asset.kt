package com.modibo.keepguard.domain.model

data class Asset(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val category: AssetCategory = AssetCategory.OTHER,
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val purchaseDate: Long? = null,
    val purchasePrice: Double? = null,
    val purchasePlace: String = "",
    val imageUrl: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
