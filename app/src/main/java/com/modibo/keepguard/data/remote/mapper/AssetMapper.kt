package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.AssetDto
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.model.AssetCondition
import com.modibo.keepguard.domain.model.AssetSubCategory
import com.modibo.keepguard.domain.model.PurchaseMode

fun AssetDto.toDomain(id: String) = Asset(
    id = id,
    userId = userId,
    name = name,
    description = description,
    category = AssetCategory.valueOf(category),
    subCategory = subCategory?.let { AssetSubCategory.valueOf(it) },
    purchaseMode = PurchaseMode.valueOf(purchaseMode),
    condition = AssetCondition.valueOf(condition),
    warrantyMonths = warrantyMonths,
    brand = brand,
    model = model,
    serialNumber = serialNumber,
    purchaseDate = purchaseDate,
    purchasePrice = purchasePrice,
    purchasePlace = purchasePlace,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Asset.toDto() = AssetDto(
    userId = userId,
    name = name,
    description = description,
    category = category.name,
    subCategory = subCategory?.name,
    purchaseMode = purchaseMode.name,
    condition = condition.name,
    warrantyMonths = warrantyMonths,
    brand = brand,
    model = model,
    serialNumber = serialNumber,
    purchaseDate = purchaseDate,
    purchasePrice = purchasePrice,
    purchasePlace = purchasePlace,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)