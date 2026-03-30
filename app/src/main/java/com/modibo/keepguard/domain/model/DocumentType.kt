package com.modibo.keepguard.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DocumentType(val label: String) {
    INVOICE("Facture"),
    WARRANTY_CERT("Bon de garantie"),
    MAINTENANCE_REPORT("Facture entretien"),
    INSURANCE("Attestation assurance"),
    MANUAL("Notice / Manuel"),
    OTHER("Autre")
}