package com.modibo.keepguard.domain.model

data class ConsumerRight(
    val name: String,
    val description: String,
    val source: String,
    val type: RightType,
    val durationMonths: Int? = null,
    val modes: List<PurchaseMode> = PurchaseMode.entries,
    val conditions: List<AssetCondition> = AssetCondition.entries,
    val minPurchaseDate: String? = null
)
