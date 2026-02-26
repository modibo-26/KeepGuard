package com.modibo.keepguard.domain.model

enum class WarrantyStatus(val label: String) {
    ACTIVE("Active"),                     // > 30 jours restants
    EXPIRING_SOON("Expire bientôt"),      // ≤ 30 jours restants
    EXPIRED("Expirée")                    // Passée
}

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