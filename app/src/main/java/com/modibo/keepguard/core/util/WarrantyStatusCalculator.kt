package com.modibo.keepguard.core.util

import com.modibo.keepguard.domain.model.WarrantyStatus

object WarrantyStatusCalculator {
    fun calculate(endDate: Long): WarrantyStatus {
        val now = System.currentTimeMillis()
        val daysLeft = (endDate - now) / (1000 * 60 * 60 * 24)
        return when {
            daysLeft <= 0 -> WarrantyStatus.EXPIRED
            daysLeft <= 30 -> WarrantyStatus.EXPIRING_SOON
            else -> WarrantyStatus.ACTIVE
        }
    }
}