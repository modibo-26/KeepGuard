package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.AssetDto
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.model.AssetCondition
import com.modibo.keepguard.domain.model.AssetSubCategory
import com.modibo.keepguard.domain.model.PurchaseMode
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AssetMapperTest {

    // ==================== toDomain ====================

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = AssetDto(
            userId = "user1",
            name = "TV",
            description = "Smart TV",
            category = "TECH",
            subCategory = "LAPTOP",
            purchaseMode = "ONLINE",
            condition = "NEW",
            warrantyMonths = 24,
            brand = "Samsung",
            model = "UE55",
            serialNumber = "SN123",
            purchaseDate = 1_700_000_000_000L,
            purchasePrice = 599.99,
            purchasePlace = "Amazon",
            imageUrl = "https://img.com/tv.jpg",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L
        )

        val asset = dto.toDomain("abc123")

        assertEquals("abc123", asset.id)
        assertEquals("user1", asset.userId)
        assertEquals("TV", asset.name)
        assertEquals("Smart TV", asset.description)
        assertEquals(AssetCategory.TECH, asset.category)
        assertEquals(AssetSubCategory.LAPTOP, asset.subCategory)
        assertEquals(PurchaseMode.ONLINE, asset.purchaseMode)
        assertEquals(AssetCondition.NEW, asset.condition)
        assertEquals(24, asset.warrantyMonths)
        assertEquals("Samsung", asset.brand)
        assertEquals("UE55", asset.model)
        assertEquals("SN123", asset.serialNumber)
        assertEquals(1_700_000_000_000L, asset.purchaseDate)
        assertEquals(599.99, asset.purchasePrice)
        assertEquals("Amazon", asset.purchasePlace)
        assertEquals("https://img.com/tv.jpg", asset.imageUrl)
    }

    @Test
    fun `toDomain maps null subCategory`() {
        val dto = AssetDto(category = "OTHER", purchaseMode = "UNKNOWN", condition = "NEW")
        val asset = dto.toDomain("id")
        assertEquals(null, asset.subCategory)
    }

    @Test
    fun `toDomain falls back to OTHER on invalid category`() {
        val dto = AssetDto(category = "INVALID", purchaseMode = "UNKNOWN", condition = "NEW")
        val asset = dto.toDomain("id")
        assertEquals(AssetCategory.OTHER, asset.category)
    }

    @Test
    fun `toDomain falls back to null on invalid subCategory`() {
        val dto = AssetDto(category = "OTHER", subCategory = "INVALID", purchaseMode = "UNKNOWN", condition = "NEW")
        val asset = dto.toDomain("id")
        assertEquals(null, asset.subCategory)
    }

    @Test
    fun `toDomain falls back to UNKNOWN on invalid purchaseMode`() {
        val dto = AssetDto(category = "OTHER", purchaseMode = "INVALID", condition = "NEW")
        val asset = dto.toDomain("id")
        assertEquals(PurchaseMode.UNKNOWN, asset.purchaseMode)
    }

    @Test
    fun `toDomain falls back to NEW on invalid condition`() {
        val dto = AssetDto(category = "OTHER", purchaseMode = "UNKNOWN", condition = "INVALID")
        val asset = dto.toDomain("id")
        assertEquals(AssetCondition.NEW, asset.condition)
    }

    // ==================== toDto ====================

    @Test
    fun `toDto maps all fields correctly`() {
        val asset = Asset(
            userId = "user1",
            name = "TV",
            description = "Smart TV",
            category = AssetCategory.TECH,
            subCategory = AssetSubCategory.LAPTOP,
            purchaseMode = PurchaseMode.ONLINE,
            condition = AssetCondition.NEW,
            warrantyMonths = 24,
            brand = "Samsung",
            model = "UE55",
            serialNumber = "SN123",
            purchaseDate = 1_700_000_000_000L,
            purchasePrice = 599.99,
            purchasePlace = "Amazon",
            imageUrl = "https://img.com/tv.jpg",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L
        )

        val dto = asset.toDto()

        assertEquals("user1", dto.userId)
        assertEquals("TV", dto.name)
        assertEquals("TECH", dto.category)
        assertEquals("LAPTOP", dto.subCategory)
        assertEquals("ONLINE", dto.purchaseMode)
        assertEquals("NEW", dto.condition)
        assertEquals(24, dto.warrantyMonths)
        assertEquals("Samsung", dto.brand)
    }

    @Test
    fun `toDto maps null subCategory to null`() {
        val asset = Asset(subCategory = null)
        val dto = asset.toDto()
        assertEquals(null, dto.subCategory)
    }

    // ==================== Round trip ====================

    @Test
    fun `toDomain then toDto preserves data`() {
        val dto = AssetDto(
            userId = "user1", name = "TV", category = "TECH",
            purchaseMode = "ONLINE", condition = "NEW", brand = "Samsung"
        )
        val result = dto.toDomain("id").toDto()
        assertEquals(dto.name, result.name)
        assertEquals(dto.category, result.category)
        assertEquals(dto.purchaseMode, result.purchaseMode)
        assertEquals(dto.condition, result.condition)
        assertEquals(dto.brand, result.brand)
    }
}
