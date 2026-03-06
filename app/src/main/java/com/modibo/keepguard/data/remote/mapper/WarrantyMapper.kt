package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.WarrantyDto
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType

fun WarrantyDto.toDomain(id: String) = Warranty(
    id = id,
    assetId  = assetId,
    userId = userId,
    type =  try {
        WarrantyType.valueOf(type)
    } catch (e: Exception) {
        WarrantyType.MANUFACTURER
    },
    startDate = startDate,
    durationMonths = durationMonths,
    endDate = endDate,
    provider = provider,
    conditions = conditions,
    documentIds = documentIds,
    createdAt = createdAt
)

fun Warranty.toDto() = WarrantyDto(
    assetId  = assetId,
    userId = userId,
    type = type.name,
    startDate = startDate,
    durationMonths = durationMonths,
    endDate = endDate,
    provider = provider,
    conditions = conditions,
    documentIds = documentIds,
    createdAt = createdAt
)

