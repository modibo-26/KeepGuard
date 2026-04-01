package com.modibo.keepguard.presentation.screen.document.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.model.DocumentType
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase
import com.modibo.keepguard.domain.usecase.document.AddDocumentUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByUserUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantiesByUserUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentFormViewModelTest {

    private lateinit var addDocument: AddDocumentUseCase
    private lateinit var getAssets: GetAssetsUseCase
    private lateinit var getWarranties: GetWarrantiesByUserUseCase
    private lateinit var getMaintenances: GetMaintenanceByUserUseCase

    private lateinit var viewModel: DocumentFormViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        addDocument = mockk()
        getAssets = mockk()
        getWarranties = mockk()
        getMaintenances = mockk()

        mockkStatic(Uri::class)
        every { Uri.decode(any()) } returns ""
        every { Uri.encode(any()) } returns ""

        every { getAssets() } returns flowOf(Resource.Success(emptyList()))
        every { getWarranties() } returns flowOf(Resource.Success(emptyList()))
        every { getMaintenances() } returns flowOf(Resource.Success(emptyList()))

        viewModel = DocumentFormViewModel(
            addDocument,
            getAssets,
            getWarranties,
            getMaintenances,
            SavedStateHandle()
        )
    }

    // ==================== Navigation ====================

    @Test
    fun `nextStep from SOURCE goes to INFO`() {
        viewModel.nextStep()
        assertEquals(DocumentFormStep.INFO, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from INFO goes to LINK`() {
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(DocumentFormStep.LINK, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from LINK goes to RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(DocumentFormStep.RECAP, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from RECAP stays RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(DocumentFormStep.RECAP, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from INFO goes to SOURCE when not from scanner`() {
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(DocumentFormStep.SOURCE, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from INFO stays INFO when from scanner`() = runTest {
        every { Uri.decode(any()) } answers { firstArg() }

        val scannedData = ScannedData(name = "Facture")
        val jsonString = Json.encodeToString(ScannedData.serializer(), scannedData)

        val scannerViewModel = DocumentFormViewModel(
            addDocument, getAssets, getWarranties, getMaintenances,
            SavedStateHandle(mapOf(
                "scannedJson" to jsonString,
                "imageUri" to "fakeUri"
            ))
        )
        advanceUntilIdle()

        scannerViewModel.prevStep()
        assertEquals(DocumentFormStep.INFO, scannerViewModel.state.value.step)
    }

    @Test
    fun `prevStep from LINK goes to INFO`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(DocumentFormStep.INFO, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from RECAP goes to LINK`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(DocumentFormStep.LINK, viewModel.state.value.step)
    }

    // ==================== Champs simples ====================

    @Test
    fun `onNameChange updates name`() {
        viewModel.onNameChange("Facture EDF")
        assertEquals("Facture EDF", viewModel.state.value.name)
    }

    @Test
    fun `onTypeChange updates type`() {
        viewModel.onTypeChange(DocumentType.INVOICE)
        assertEquals(DocumentType.INVOICE, viewModel.state.value.type)
    }

    @Test
    fun `onDateChange updates date`() {
        viewModel.onDateChange("2026-01-15")
        assertEquals("2026-01-15", viewModel.state.value.date)
    }

    @Test
    fun `onAmountChange updates amount`() {
        viewModel.onAmountChange("59.99")
        assertEquals("59.99", viewModel.state.value.amount)
    }

    @Test
    fun `onMerchantChange updates merchant`() {
        viewModel.onMerchantChange("Amazon")
        assertEquals("Amazon", viewModel.state.value.merchant)
    }

    @Test
    fun `onAssetSelected updates assetId`() {
        viewModel.onAssetSelected("asset123")
        assertEquals("asset123", viewModel.state.value.assetId)
    }

    @Test
    fun `onWarrantySelected updates warrantyId`() {
        viewModel.onWarrantySelected("warranty123")
        assertEquals("warranty123", viewModel.state.value.warrantyId)
    }

    @Test
    fun `onMaintenanceSelected updates maintenanceId`() {
        viewModel.onMaintenanceSelected("maintenance123")
        assertEquals("maintenance123", viewModel.state.value.maintenanceId)
    }

    @Test
    fun `onFileSelected updates fileUri`() {
        val uri = mockk<Uri>()
        viewModel.onFileSelected(uri)
        assertEquals(uri, viewModel.state.value.fileUri)
    }

    // ==================== saveDocument ====================

    @Test
    fun `saveDocument calls addDocument with fileUri`() = runTest {
        val document = Document(id = "newId")
        every { addDocument(any(), any()) } returns flowOf(Resource.Success(document))

        val uri = mockk<Uri>()
        viewModel.onFileSelected(uri)

        viewModel.saveDocument()
        advanceUntilIdle()

        verify { addDocument(any(), any()) }
    }

    @Test
    fun `saveDocument sets isSaved true after Success`() = runTest {
        val document = Document(id = "newId")
        every { addDocument(any(), any()) } returns flowOf(Resource.Success(document))

        val uri = mockk<Uri>()
        viewModel.onFileSelected(uri)

        viewModel.saveDocument()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isSaved)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveDocument sets isLoading true during Loading`() = runTest {
        every { addDocument(any(), any()) } returns flowOf(Resource.Loading())

        val uri = mockk<Uri>()
        viewModel.onFileSelected(uri)

        viewModel.saveDocument()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveDocument sets error after Error`() = runTest {
        every { addDocument(any(), any()) } returns flowOf(Resource.Error("erreur test"))

        val uri = mockk<Uri>()
        viewModel.onFileSelected(uri)

        viewModel.saveDocument()
        advanceUntilIdle()

        assertEquals("erreur test", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveDocument returns early when fileUri is null`() = runTest {
        every { addDocument(any(), any()) } returns flowOf(Resource.Success(Document()))

        viewModel.saveDocument()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isSaved)
        verify(exactly = 0) { addDocument(any(), any()) }
    }

    // ==================== loadAssets ====================

    @Test
    fun `loadAssets fills assets after Success`() = runTest {
        val assets = listOf(Asset(id = "1", name = "TV"), Asset(id = "2", name = "Frigo"))
        every { getAssets() } returns flowOf(Resource.Success(assets))

        viewModel.loadAssets()
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.assets.size)
    }

    // ==================== Init ====================

    @Test
    fun `init prefills fields from scannedJson`() = runTest {
        every { Uri.decode(any()) } answers { firstArg() }

        val scannedData = ScannedData(
            name = "Facture Amazon",
            type = DocumentType.INVOICE,
            date = "2026-01-15",
            amount = "599.99",
            merchant = "Amazon"
        )
        val jsonString = Json.encodeToString(ScannedData.serializer(), scannedData)

        val scannerViewModel = DocumentFormViewModel(
            addDocument, getAssets, getWarranties, getMaintenances,
            SavedStateHandle(mapOf(
                "scannedJson" to jsonString,
                "imageUri" to "fakeUri"
            ))
        )

        assertEquals("Facture Amazon", scannerViewModel.state.value.name)
        assertEquals(DocumentType.INVOICE, scannerViewModel.state.value.type)
        assertEquals("2026-01-15", scannerViewModel.state.value.date)
        assertEquals("599.99", scannerViewModel.state.value.amount)
        assertEquals("Amazon", scannerViewModel.state.value.merchant)
        assertEquals(true, scannerViewModel.state.value.fromScanner)
        assertEquals(DocumentFormStep.INFO, scannerViewModel.state.value.step)
    }

    @Test
    fun `init stays at SOURCE when no scannedJson`() {
        assertEquals(false, viewModel.state.value.fromScanner)
        assertEquals(DocumentFormStep.SOURCE, viewModel.state.value.step)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }
}
