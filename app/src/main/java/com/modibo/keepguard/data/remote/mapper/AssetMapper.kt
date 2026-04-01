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
    category = try { AssetCategory.valueOf(category) } catch (e: Exception) { AssetCategory.OTHER },
    subCategory = subCategory?.let { try { AssetSubCategory.valueOf(it) } catch (e: Exception) { null } },
    purchaseMode = try { PurchaseMode.valueOf(purchaseMode) } catch (e: Exception) { PurchaseMode.UNKNOWN },
    condition = try { AssetCondition.valueOf(condition) } catch (e: Exception) { AssetCondition.NEW },
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