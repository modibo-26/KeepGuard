package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.MaintenanceDto
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.model.MaintenanceType
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MaintenanceMapperTest {

    // ==================== toDomain ====================

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = MaintenanceDto(
            assetId = "asset1", userId = "user1", title = "Vidange",
            description = "Huile 5W30", type = "RECURRING",
            date = 1_700_000_000_000L, cost = 150.0, provider = "Garage Auto",
            mileage = 50000, isCompleted = true, recurrenceMonths = 6,
            createdAt = 1_700_000_000_000L
        )

        val maintenance = dto.toDomain("m123")

        assertEquals("m123", maintenance.id)
        assertEquals("asset1", maintenance.assetId)
        assertEquals("Vidange", maintenance.title)
        assertEquals(MaintenanceType.RECURRING, maintenance.type)
        assertEquals(150.0, maintenance.cost)
        assertEquals(50000, maintenance.mileage)
        assertEquals(true, maintenance.isCompleted)
        assertEquals(6, maintenance.recurrenceMonths)
    }

    @Test
    fun `toDomain falls back to ONE_TIME on invalid type`() {
        val dto = MaintenanceDto(type = "INVALID")
        val maintenance = dto.toDomain("id")
        assertEquals(MaintenanceType.ONE_TIME, maintenance.type)
    }

    // ==================== toDto ====================

    @Test
    fun `toDto maps all fields correctly`() {
        val maintenance = Maintenance(
            assetId = "asset1", title = "Vidange",
            type = MaintenanceType.RECURRING, cost = 150.0,
            mileage = 50000, isCompleted = true, recurrenceMonths = 6
        )

        val dto = maintenance.toDto()

        assertEquals("asset1", dto.assetId)
        assertEquals("Vidange", dto.title)
        assertEquals("RECURRING", dto.type)
        assertEquals(150.0, dto.cost)
        assertEquals(50000, dto.mileage)
        assertEquals(true, dto.isCompleted)
        assertEquals(6, dto.recurrenceMonths)
    }

    // ==================== Round trip ====================

    @Test
    fun `toDomain then toDto preserves data`() {
        val dto = MaintenanceDto(
            assetId = "asset1", title = "Révision",
            type = "ONE_TIME", cost = 200.0, isCompleted = false
        )
        val result = dto.toDomain("id").toDto()
        assertEquals(dto.assetId, result.assetId)
        assertEquals(dto.title, result.title)
        assertEquals(dto.type, result.type)
        assertEquals(dto.cost, result.cost)
        assertEquals(dto.isCompleted, result.isCompleted)
    }
}
