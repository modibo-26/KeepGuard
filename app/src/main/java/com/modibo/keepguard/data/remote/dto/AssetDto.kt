package com.modibo.keepguard.data.remote.dto

data class AssetDto(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
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
