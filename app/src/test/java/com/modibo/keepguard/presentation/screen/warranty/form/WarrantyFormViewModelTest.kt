package com.modibo.keepguard.presentation.screen.warranty.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.worker.ReminderScheduler
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
import com.modibo.keepguard.domain.usecase.warranty.AddWarrantyUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantyByIdUseCase
import com.modibo.keepguard.domain.usecase.warranty.UpdateWarrantyUseCase
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
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WarrantyFormViewModelTest {

    private lateinit var addWarranty: AddWarrantyUseCase
    private lateinit var updateWarranty: UpdateWarrantyUseCase
    private lateinit var getWarrantyById: GetWarrantyByIdUseCase
    private lateinit var parseDocument: ParseDocumentUseCase
    private lateinit var scheduler: ReminderScheduler

    private lateinit var viewModel: WarrantyFormViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        addWarranty = mockk()
        updateWarranty = mockk()
        getWarrantyById = mockk()
        parseDocument = mockk()
        scheduler = mockk(relaxed = true)

        mockkStatic(Uri::class)
        every { Uri.decode(any()) } returns ""
        every { Uri.encode(any()) } returns ""

        viewModel = WarrantyFormViewModel(
            addWarranty,
            updateWarranty,
            getWarrantyById,
            parseDocument,
            scheduler,
            SavedStateHandle()
        )
    }

    // ==================== Navigation ====================

    @Test
    fun `nextStep from SOURCE goes to INFO`() {
        viewModel.nextStep()
        assertEquals(WarrantyFormStep.INFO, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from INFO goes to RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(WarrantyFormStep.RECAP, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from RECAP stays RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(WarrantyFormStep.RECAP, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from INFO goes to SOURCE when not editing`() {
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(WarrantyFormStep.SOURCE, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from INFO stays INFO when editing`() = runTest {
        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        editViewModel.prevStep()
        assertEquals(WarrantyFormStep.INFO, editViewModel.state.value.step)
    }

    @Test
    fun `prevStep from RECAP goes to INFO`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(WarrantyFormStep.INFO, viewModel.state.value.step)
    }

    // ==================== Champs simples ====================

    @Test
    fun `onTypeChange updates type`() {
        viewModel.onTypeChange(WarrantyType.SELLER)
        assertEquals(WarrantyType.SELLER, viewModel.state.value.type)
    }

    @Test
    fun `onStartDateChange updates startDate`() {
        viewModel.onStartDateChange(1_700_000_000_000L)
        assertEquals(1_700_000_000_000L, viewModel.state.value.startDate)
    }

    @Test
    fun `onDurationChange updates durationMonths`() {
        viewModel.onDurationChange("12")
        assertEquals("12", viewModel.state.value.durationMonths)
    }

    @Test
    fun `onProviderChange updates provider`() {
        viewModel.onProviderChange("test")
        assertEquals("test", viewModel.state.value.provider)
    }

    @Test
    fun `onConditionsChange updates conditions`() {
        viewModel.onConditionsChange("neuf")
        assertEquals("neuf", viewModel.state.value.conditions)
    }

    // ==================== saveWarranty — création ====================

    @Test
    fun `saveWarranty calls addWarranty when warrantyId is empty`() = runTest {
        mockSaveWarrantySuccess()

        viewModel.onStartDateChange(1_700_000_000_000L)
        viewModel.onDurationChange("24")

        viewModel.saveWarranty()
        advanceUntilIdle()

        verify { addWarranty(any()) }
        verify(exactly = 0) { updateWarranty(any()) }
    }

    @Test
    fun `saveWarranty sets isSaved true after Success`() = runTest {
        mockSaveWarrantySuccess()

        viewModel.onStartDateChange(1_700_000_000_000L)
        viewModel.onDurationChange("24")

        viewModel.saveWarranty()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isSaved)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveWarranty sets isLoading true during Loading`() = runTest {
        every { addWarranty(any()) } returns flowOf(Resource.Loading())

        viewModel.onStartDateChange(1_700_000_000_000L)
        viewModel.onDurationChange("24")

        viewModel.saveWarranty()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveWarranty sets error after Error`() = runTest {
        every { addWarranty(any()) } returns flowOf(Resource.Error("erreur test"))

        viewModel.onStartDateChange(1_700_000_000_000L)
        viewModel.onDurationChange("24")

        viewModel.saveWarranty()
        advanceUntilIdle()

        assertEquals("erreur test", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveWarranty returns early when startDate is null`() = runTest {
        mockSaveWarrantySuccess()

        viewModel.saveWarranty()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isSaved)
        assertEquals(false, viewModel.state.value.isLoading)
        verify(exactly = 0) { addWarranty(any()) }
    }

    @Test
    fun `saveWarranty schedules reminder on Success`() = runTest {
        mockSaveWarrantySuccess()

        viewModel.onStartDateChange(1_700_000_000_000L)
        viewModel.onDurationChange("24")

        viewModel.saveWarranty()
        advanceUntilIdle()

        verify { scheduler.schedule(any(), any(), any(), any()) }
    }

    // ==================== saveWarranty — édition ====================

    @Test
    fun `saveWarranty calls updateWarranty when warrantyId is not empty`() = runTest {
        val warranty = Warranty(id = "123", assetId = "abc")
        every { getWarrantyById("123") } returns flowOf(Resource.Success(warranty))
        every { updateWarranty(any()) } returns flowOf(Resource.Success(warranty))

        val editViewModel = WarrantyFormViewModel(
            addWarranty, updateWarranty, getWarrantyById, parseDocument, scheduler,
            SavedStateHandle(mapOf("warrantyId" to "123"))
        )
        advanceUntilIdle()

        editViewModel.onStartDateChange(1_700_000_000_000L)
        editViewModel.onDurationChange("12")

        editViewModel.saveWarranty()
        advanceUntilIdle()

        verify { updateWarranty(any()) }
        verify(exactly = 0) { addWarranty(any()) }
    }

    @Test
    fun `saveWarranty schedules reminder on Success in edit mode`() = runTest {
        val warranty = Warranty(id = "123", assetId = "abc")
        every { getWarrantyById("123") } returns flowOf(Resource.Success(warranty))
        every { updateWarranty(any()) } returns flowOf(Resource.Success(warranty))

        val editViewModel = WarrantyFormViewModel(
            addWarranty, updateWarranty, getWarrantyById, parseDocument, scheduler,
            SavedStateHandle(mapOf("warrantyId" to "123"))
        )
        advanceUntilIdle()

        editViewModel.onStartDateChange(1_700_000_000_000L)
        editViewModel.onDurationChange("12")

        editViewModel.saveWarranty()
        advanceUntilIdle()

        verify { scheduler.schedule(any(), any(), any(), any()) }
    }

    // ==================== loadWarranty ====================

    @Test
    fun `loadWarranty sets isLoading true during Loading`() = runTest {
        every { getWarrantyById("123") } returns flowOf(Resource.Loading())

        val editViewModel = WarrantyFormViewModel(
            addWarranty, updateWarranty, getWarrantyById, parseDocument, scheduler,
            SavedStateHandle(mapOf("warrantyId" to "123"))
        )
        advanceUntilIdle()

        assertEquals(true, editViewModel.state.value.isLoading)
    }

    @Test
    fun `loadWarranty fills state after Success`() = runTest {
        val warranty = Warranty(
            id = "123", assetId = "abc", type = WarrantyType.SELLER,
            startDate = 1_700_000_000_000L, durationMonths = 12,
            provider = "Samsung", conditions = "Neuf uniquement"
        )
        every { getWarrantyById("123") } returns flowOf(Resource.Success(warranty))

        val editViewModel = WarrantyFormViewModel(
            addWarranty, updateWarranty, getWarrantyById, parseDocument, scheduler,
            SavedStateHandle(mapOf("warrantyId" to "123"))
        )
        advanceUntilIdle()

        assertEquals(false, editViewModel.state.value.isLoading)
        assertEquals(WarrantyType.SELLER, editViewModel.state.value.type)
        assertEquals(1_700_000_000_000L, editViewModel.state.value.startDate)
        assertEquals("12", editViewModel.state.value.durationMonths)
        assertEquals("Samsung", editViewModel.state.value.provider)
        assertEquals("Neuf uniquement", editViewModel.state.value.conditions)
    }

    @Test
    fun `loadWarranty sets error after Error`() = runTest {
        every { getWarrantyById("123") } returns flowOf(Resource.Error("fetch error"))

        val editViewModel = WarrantyFormViewModel(
            addWarranty, updateWarranty, getWarrantyById, parseDocument, scheduler,
            SavedStateHandle(mapOf("warrantyId" to "123"))
        )
        advanceUntilIdle()

        assertEquals(false, editViewModel.state.value.isLoading)
        assertEquals("fetch error", editViewModel.state.value.error)
    }

    // ==================== onScanResult ====================

    @Test
    fun `onScanResult sets isLoading true during Loading`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Loading())

        val uri = mockk<Uri>()
        viewModel.onScanResult(uri)
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `onScanResult updates state with scanned data`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Success(ScannedData(
            warrantyType = WarrantyType.SELLER,
            durationMonths = 36,
            warrantyProvider = "Apple",
            conditions = "AppleCare+"
        )))

        val uri = mockk<Uri>()
        viewModel.onScanResult(uri)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(WarrantyType.SELLER, viewModel.state.value.type)
        assertEquals("36", viewModel.state.value.durationMonths)
        assertEquals("Apple", viewModel.state.value.provider)
        assertEquals("AppleCare+", viewModel.state.value.conditions)
        assertEquals(true, viewModel.state.value.fromScanner)
    }

    @Test
    fun `onScanResult keeps existing values when scanned data is empty`() = runTest {
        viewModel.onProviderChange("Existing Provider")
        viewModel.onConditionsChange("Existing Conditions")

        every { parseDocument(any()) } returns flowOf(Resource.Success(ScannedData(
            durationMonths = 0,
            warrantyProvider = "",
            conditions = ""
        )))

        val uri = mockk<Uri>()
        viewModel.onScanResult(uri)
        advanceUntilIdle()

        assertEquals("Existing Provider", viewModel.state.value.provider)
        assertEquals("Existing Conditions", viewModel.state.value.conditions)
        assertEquals("24", viewModel.state.value.durationMonths)
    }

    @Test
    fun `onScanResult sets error after Error`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Error("scan error"))

        val uri = mockk<Uri>()
        viewModel.onScanResult(uri)
        advanceUntilIdle()

        assertEquals("scan error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== Init ====================

    @Test
    fun `init sets isEditing true and step INFO when warrantyId is not empty`() = runTest {
        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        assertEquals(true, editViewModel.state.value.isEditing)
        assertEquals(WarrantyFormStep.INFO, editViewModel.state.value.step)
    }

    @Test
    fun `init stays at SOURCE when warrantyId is empty`() {
        assertEquals(false, viewModel.state.value.isEditing)
        assertEquals(WarrantyFormStep.SOURCE, viewModel.state.value.step)
    }

    // ==================== Helpers ====================

    private fun mockSaveWarrantySuccess(): Warranty {
        val warranty = Warranty(id = "newId", assetId = "abc")
        every { addWarranty(any()) } returns flowOf(Resource.Success(warranty))
        return warranty
    }

    private fun createEditViewModel(warrantyId: String = "123"): WarrantyFormViewModel {
        val warranty = Warranty(id = warrantyId, assetId = "abc")
        every { getWarrantyById(warrantyId) } returns flowOf(Resource.Success(warranty))
        return WarrantyFormViewModel(
            addWarranty, updateWarranty, getWarrantyById, parseDocument, scheduler,
            SavedStateHandle(mapOf("warrantyId" to warrantyId))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }
}
