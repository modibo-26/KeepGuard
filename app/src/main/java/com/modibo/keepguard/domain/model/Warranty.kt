package com.modibo.keepguard.domain.model

data class Warranty(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val type: WarrantyType = WarrantyType.MANUFACTURER,
    val startDate: Long = 0,
    val durationMonths: Int = 24,
    val endDate: Long = 0,
    val provider: String = "",
    val conditions: String = "",
    val documentIds: List<String> = emptyList(),
    val createdAt: Long = 0
)

//// Statut calculé dynamiquement — jamais stocké dans la data class
//val Warranty.status: WarrantyStatus
//    get() = WarrantyStatusCalculator.calculate(endDate)// Passée
