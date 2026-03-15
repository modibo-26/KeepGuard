package com.modibo.keepguard.core.util

import org.w3c.dom.Entity

object Constants {
    const val REMINDER_DAYS_BEFORE = 30L
    const val REMINDER_OFFSET_MILLIS = REMINDER_DAYS_BEFORE * 24 * 60 * 60 * 1000

    object ErrorMessages {
        fun addError(entity: String) = "Erreur d'ajout : $entity"
        fun fetchError(entity: String) = "Erreur d'ajout : $entity"
        fun deleteError(entity: String) = "Erreur d'ajout : $entity"
        fun updateError(entity: String) = "Erreur d'ajout : $entity"
        const val NOT_AUTHENTICATED = "Non connecté"

    }
}