package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.AssetDto
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory

fun AssetDto.toDomain(id: String) = Asset(
    id = id,
    userId = userId,
    name = name,
    description = description,
    category = try {
        AssetCategory.valueOf(category)
    } catch (e: Exception) {
        AssetCategory.OTHER
    },
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