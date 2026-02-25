package com.modibo.keepguard.domain.model

data class Maintenance(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val type: MaintenanceType = MaintenanceType.ONE_TIME,
    val date: Long = 0,
    val cost: Double? = null,
    val provider: String = "",
    val mileage: Int? = null,
    val isCompleted: Boolean = false,
    val recurrenceMonths: Int? = null,
    val nextDueDate: Long? = null,
    val nextDueMileage: Int? = null,
    val documentIds: List<String> = emptyList(),
    val createdAt: Long = 0
)
