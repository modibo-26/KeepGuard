package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.MaintenanceDto
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.model.MaintenanceType

fun MaintenanceDto.toDomain(id: String) = Maintenance(
    id = id,
    assetId  = assetId,
    userId = userId,
    title = title,
    description = description,
    type =  try {
        MaintenanceType.valueOf(type)
    } catch (e: Exception) {
        MaintenanceType.ONE_TIME
    },
    date = date,
    cost = cost,
    provider = provider,
    mileage = mileage,
    isCompleted = isCompleted,
    recurrenceMonths = recurrenceMonths,
    nextDueDate = nextDueDate,
    nextDueMileage = nextDueMileage,
    documentIds = documentIds,
    createdAt = createdAt
)

fun Maintenance.toDto() = MaintenanceDto(
    assetId  = assetId,
    userId = userId,
    title = title,
    description = description,
    type =  type.name,
    date = date,
    cost = cost,
    provider = provider,
    mileage = mileage,
    isCompleted = isCompleted,
    recurrenceMonths = recurrenceMonths,
    nextDueDate = nextDueDate,
    nextDueMileage = nextDueMileage,
    documentIds = documentIds,
    createdAt = createdAt
)