package com.modibo.keepguard.domain.model

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val isAnonymous: Boolean = true,
    val householdId: String? = null,      // V2 : partage foyer
    val createdAt: Long = 0
)
