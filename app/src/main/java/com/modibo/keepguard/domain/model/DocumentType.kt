package com.modibo.keepguard.domain.model

enum class DocumentType(val label: String) {
    INVOICE("Facture"),
    WARRANTY_CERT("Bon de garantie"),
    MAINTENANCE_REPORT("Facture entretien"),
    INSURANCE("Attestation assurance"),
    MANUAL("Notice / Manuel"),
    OTHER("Autre")
}