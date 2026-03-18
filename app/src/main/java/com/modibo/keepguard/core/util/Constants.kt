package com.modibo.keepguard.core.util

object Constants {
    const val REMINDER_DAYS_BEFORE = 30L
    const val REMINDER_OFFSET_MILLIS = REMINDER_DAYS_BEFORE * 24 * 60 * 60 * 1000

    object Collections {
        const val ASSETS = "assets"
        const val DOCUMENTS = "documents"
        const val WARRANTIES = "warranties"
        const val MAINTENANCES = "maintenances"
    }

    object ErrorMessages {
        fun addError(entity: String) = "Erreur d'ajout : $entity"
        fun fetchError(entity: String) = "Erreur de fetch : $entity"
        fun deleteError(entity: String) = "Erreur de suppression : $entity"
        fun updateError(entity: String) = "Erreur de modification : $entity"
        const val NOT_AUTHENTICATED = "Non connecté"
        const val AUTH_ERROR = "Erreur d'authentification"
        const val LINK_ERROR = "Erreur de liaison du compte"
        const val REAUTH_ERROR = "Erreur re-authentification"
    }
}