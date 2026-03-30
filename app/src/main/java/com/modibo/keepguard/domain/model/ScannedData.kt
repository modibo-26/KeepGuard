package com.modibo.keepguard.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScannedData(
    // Document
    val name: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val date: String = "",
    val amount: String = "",
    val merchant: String = "",
    // Asset
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val warrantyMonths: Int = 0,
    val category: AssetCategory = AssetCategory.OTHER,
    val purchasePrice: String = "",
    val purchasePlace: String = "",
    val purchaseDate: String = "",
    // Warranty
    val warrantyType: WarrantyType = WarrantyType.MANUFACTURER,
    val durationMonths: Int = 0,
    val warrantyProvider: String = "",
    val conditions: String = "",
    // Maintenance
    val maintenanceTitle: String = "",
    val maintenanceDescription: String = "",
    val maintenanceType: MaintenanceType = MaintenanceType.ONE_TIME,
    val cost: String = "",
    val maintenanceProvider: String = "",
)