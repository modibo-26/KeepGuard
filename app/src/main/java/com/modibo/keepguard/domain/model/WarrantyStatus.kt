package com.modibo.keepguard.domain.model

enum class WarrantyStatus(val label: String) {
    ACTIVE("Active"),                     // > 30 jours restants
    EXPIRING_SOON("Expire bientôt"),      // ≤ 30 jours restants
    EXPIRED("Expirée")
}