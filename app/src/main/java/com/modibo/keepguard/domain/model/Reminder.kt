package com.modibo.keepguard.domain.model

data class Reminder(
    val id: String = "",
    val userId: String = "",
    val assetId: String = "",
    val referenceId: String = "",
    val referenceType: ReminderRefType = ReminderRefType.WARRANTY,
    val title: String = "",
    val message: String = "",
    val triggerDate: Long = 0,
    val isTriggered: Boolean = false,
    val daysBefore: Int = 30,             // J-30, J-7
    val createdAt: Long = 0
)
