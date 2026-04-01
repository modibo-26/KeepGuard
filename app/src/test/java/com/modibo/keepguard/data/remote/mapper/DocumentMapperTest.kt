package com.modibo.keepguard.data.remote.mapper

import com.modibo.keepguard.data.remote.dto.DocumentDto
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.DocumentType
import junit.framework.TestCase.assertEquals
import org.junit.Test

class DocumentMapperTest {

    // ==================== toDomain ====================

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = DocumentDto(
            assetId = "asset1", warrantyId = "w1", maintenanceId = "m1",
            userId = "user1", name = "Facture", type = "INVOICE",
            fileUrl = "https://file.com/f.pdf", mimeType = "application/pdf",
            fileSize = 1024, date = "2026-01-15", amount = "599.99",
            merchant = "Amazon", ocrText = "texte OCR", createdAt = 1_700_000_000_000L
        )

        val document = dto.toDomain("d123")

        assertEquals("d123", document.id)
        assertEquals("asset1", document.assetId)
        assertEquals("w1", document.warrantyId)
        assertEquals("m1", document.maintenanceId)
        assertEquals("Facture", document.name)
        assertEquals(DocumentType.INVOICE, document.type)
        assertEquals("https://file.com/f.pdf", document.fileUrl)
        assertEquals("599.99", document.amount)
        assertEquals("Amazon", document.merchant)
        assertEquals("texte OCR", document.ocrText)
    }

    @Test
    fun `toDomain falls back to OTHER on invalid type`() {
        val dto = DocumentDto(type = "INVALID")
        val document = dto.toDomain("id")
        assertEquals(DocumentType.OTHER, document.type)
    }

    @Test
    fun `toDomain maps null ocrText`() {
        val dto = DocumentDto(type = "OTHER", ocrText = null)
        val document = dto.toDomain("id")
        assertEquals(null, document.ocrText)
    }

    // ==================== toDto ====================

    @Test
    fun `toDto maps all fields correctly`() {
        val document = Document(
            assetId = "asset1", warrantyId = "w1",
            name = "Facture", type = DocumentType.INVOICE,
            amount = "599.99", merchant = "Amazon"
        )

        val dto = document.toDto()

        assertEquals("asset1", dto.assetId)
        assertEquals("w1", dto.warrantyId)
        assertEquals("Facture", dto.name)
        assertEquals("INVOICE", dto.type)
        assertEquals("599.99", dto.amount)
        assertEquals("Amazon", dto.merchant)
    }

    // ==================== Round trip ====================

    @Test
    fun `toDomain then toDto preserves data`() {
        val dto = DocumentDto(
            assetId = "asset1", name = "Facture",
            type = "INVOICE", amount = "599.99", merchant = "Amazon"
        )
        val result = dto.toDomain("id").toDto()
        assertEquals(dto.assetId, result.assetId)
        assertEquals(dto.name, result.name)
        assertEquals(dto.type, result.type)
        assertEquals(dto.amount, result.amount)
        assertEquals(dto.merchant, result.merchant)
    }
}
