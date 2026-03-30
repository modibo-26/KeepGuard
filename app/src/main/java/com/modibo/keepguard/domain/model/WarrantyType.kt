package com.modibo.keepguard.domain.model

enum class WarrantyType(val label: String) {
    MANUFACTURER("Constructeur"),
    EXTENDED("Extension"),
    INSURANCE("Assurance"),
    SELLER("Vendeur"),
    LEGAL("Garantie légale"),
    RETRACTATION("Droit de rétractation")
}