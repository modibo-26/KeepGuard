package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.WarrantyDto
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType
import junit.framework.TestCase.assertEquals
import org.junit.Test

class WarrantyMapperTest {

    // ==================== toDomain ====================

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = WarrantyDto(
            assetId = "asset1", userId = "user1", type = "MANUFACTURER",
            startDate = 1_700_000_000_000L, durationMonths = 24,
            endDate = 1_763_000_000_000L, provider = "Samsung",
            conditions = "Neuf", documentIds = listOf("doc1"), createdAt = 1_700_000_000_000L
        )

        val warranty = dto.toDomain("w123")

        assertEquals("w123", warranty.id)
        assertEquals("asset1", warranty.assetId)
        assertEquals(WarrantyType.MANUFACTURER, warranty.type)
        assertEquals(24, warranty.durationMonths)
        assertEquals("Samsung", warranty.provider)
        assertEquals("Neuf", warranty.conditions)
    }

    @Test
    fun `toDomain falls back to MANUFACTURER on invalid type`() {
        val dto = WarrantyDto(type = "INVALID")
        val warranty = dto.toDomain("id")
        assertEquals(WarrantyType.MANUFACTURER, warranty.type)
    }

    // ==================== toDto ====================

    @Test
    fun `toDto maps all fields correctly`() {
        val warranty = Warranty(
            assetId = "asset1", userId = "user1", type = WarrantyType.LEGAL,
            startDate = 1_700_000_000_000L, durationMonths = 24,
            provider = "Samsung", conditions = "Neuf"
        )

        val dto = warranty.toDto()

        assertEquals("asset1", dto.assetId)
        assertEquals("LEGAL", dto.type)
        assertEquals(24, dto.durationMonths)
        assertEquals("Samsung", dto.provider)
    }

    // ==================== Round trip ====================

    @Test
    fun `toDomain then toDto preserves data`() {
        val dto = WarrantyDto(
            assetId = "asset1", type = "SELLER",
            durationMonths = 12, provider = "Fnac"
        )
        val result = dto.toDomain("id").toDto()
        assertEquals(dto.assetId, result.assetId)
        assertEquals(dto.type, result.type)
        assertEquals(dto.durationMonths, result.durationMonths)
        assertEquals(dto.provider, result.provider)
    }
}
