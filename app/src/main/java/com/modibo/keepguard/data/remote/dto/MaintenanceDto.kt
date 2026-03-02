package com.modibo.keepguard.data.remote.dto

import com.google.firebase.firestore.PropertyName

data class MaintenanceDto(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "",
    val date: Long = 0,
    val cost: Double? = null,
    val provider: String = "",
    val mileage: Int? = null,
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val recurrenceMonths: Int? = null,
    val nextDueDate: Long? = null,
    val nextDueMileage: Int? = null,
    val documentIds: List<String> = emptyList(),
    val createdAt: Long = 0
)