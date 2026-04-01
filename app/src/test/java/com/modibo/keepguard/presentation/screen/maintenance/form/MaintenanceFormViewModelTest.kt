package com.modibo.keepguard.presentation.screen.maintenance.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.worker.ReminderScheduler
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.model.MaintenanceType
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.usecase.maintenance.AddMaintenanceUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByIdUseCase
import com.modibo.keepguard.domain.usecase.maintenance.UpdateMaintenanceUseCase
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
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
class MaintenanceFormViewModelTest {

    private lateinit var addMaintenance: AddMaintenanceUseCase
    private lateinit var updateMaintenance: UpdateMaintenanceUseCase
    private lateinit var getMaintenanceById: GetMaintenanceByIdUseCase
    private lateinit var parseDocument: ParseDocumentUseCase
    private lateinit var scheduler: ReminderScheduler

    private lateinit var viewModel: MaintenanceFormViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        addMaintenance = mockk()
        updateMaintenance = mockk()
        getMaintenanceById = mockk()
        parseDocument = mockk()
        scheduler = mockk(relaxed = true)

        mockkStatic(Uri::class)
        every { Uri.decode(any()) } returns ""
        every { Uri.encode(any()) } returns ""

        viewModel = MaintenanceFormViewModel(
            addMaintenance,
            updateMaintenance,
            getMaintenanceById,
            parseDocument,
            scheduler,
            SavedStateHandle()
        )
    }

    // ==================== Navigation ====================

    @Test
    fun `nextStep from SOURCE goes to INFO`() {
        viewModel.nextStep()
        assertEquals(MaintenanceFormStep.INFO, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from INFO goes to RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(MaintenanceFormStep.RECAP, viewModel.state.value.step)
    }

    @Test
    fun `nextStep from RECAP stays RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(MaintenanceFormStep.RECAP, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from INFO goes to SOURCE when not editing`() {
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(MaintenanceFormStep.SOURCE, viewModel.state.value.step)
    }

    @Test
    fun `prevStep from INFO stays INFO when editing`() = runTest {
        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        editViewModel.prevStep()
        assertEquals(MaintenanceFormStep.INFO, editViewModel.state.value.step)
    }

    @Test
    fun `prevStep from RECAP goes to INFO`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(MaintenanceFormStep.INFO, viewModel.state.value.step)
    }

    // ==================== Champs simples ====================

    @Test
    fun `onTitleChange updates title`() {
        viewModel.onTitleChange("Vidange")
        assertEquals("Vidange", viewModel.state.value.title)
    }

    @Test
    fun `onDescriptionChange updates description`() {
        viewModel.onDescriptionChange("test")
        assertEquals("test", viewModel.state.value.description)
    }

    @Test
    fun `onTypeChange updates type`() {
        viewModel.onTypeChange(MaintenanceType.RECURRING)
        assertEquals(MaintenanceType.RECURRING, viewModel.state.value.type)
    }

    @Test
    fun `onDateChange updates date`() {
        viewModel.onDateChange(1_700_000_000_000L)
        assertEquals(1_700_000_000_000L, viewModel.state.value.date)
    }

    @Test
    fun `onCostChange updates cost`() {
        viewModel.onCostChange("150.50")
        assertEquals("150.50", viewModel.state.value.cost)
    }

    @Test
    fun `onProviderChange updates provider`() {
        viewModel.onProviderChange("Garage Auto")
        assertEquals("Garage Auto", viewModel.state.value.provider)
    }

    @Test
    fun `onMileageChange updates mileage`() {
        viewModel.onMileageChange("50000")
        assertEquals("50000", viewModel.state.value.mileage)
    }

    @Test
    fun `onCompletedChange updates isCompleted`() {
        viewModel.onCompletedChange(true)
        assertEquals(true, viewModel.state.value.isCompleted)
    }

    @Test
    fun `onRecurrenceChange updates recurrenceMonths`() {
        viewModel.onRecurrenceChange("6")
        assertEquals("6", viewModel.state.value.recurrenceMonths)
    }

    // ==================== saveMaintenance — création ====================

    @Test
    fun `saveMaintenance calls addMaintenance when maintenanceId is empty`() = runTest {
        mockSaveMaintenanceSuccess()

        viewModel.onDateChange(1_700_000_000_000L)

        viewModel.saveMaintenance()
        advanceUntilIdle()

        verify { addMaintenance(any()) }
        verify(exactly = 0) { updateMaintenance(any()) }
    }

    @Test
    fun `saveMaintenance sets isSaved true after Success`() = runTest {
        mockSaveMaintenanceSuccess()

        viewModel.onDateChange(1_700_000_000_000L)

        viewModel.saveMaintenance()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isSaved)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveMaintenance sets isLoading true during Loading`() = runTest {
        every { addMaintenance(any()) } returns flowOf(Resource.Loading())

        viewModel.onDateChange(1_700_000_000_000L)

        viewModel.saveMaintenance()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveMaintenance sets error after Error`() = runTest {
        every { addMaintenance(any()) } returns flowOf(Resource.Error("erreur test"))

        viewModel.onDateChange(1_700_000_000_000L)

        viewModel.saveMaintenance()
        advanceUntilIdle()

        assertEquals("erreur test", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveMaintenance returns early when date is null`() = runTest {
        mockSaveMaintenanceSuccess()

        viewModel.saveMaintenance()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isSaved)
        verify(exactly = 0) { addMaintenance(any()) }
    }

    @Test
    fun `saveMaintenance schedules reminder on Success`() = runTest {
        mockSaveMaintenanceSuccess()

        viewModel.onDateChange(1_700_000_000_000L)

        viewModel.saveMaintenance()
        advanceUntilIdle()

        verify { scheduler.schedule(any(), any(), any(), any()) }
    }

    // ==================== saveMaintenance — édition ====================

    @Test
    fun `saveMaintenance calls updateMaintenance when maintenanceId is not empty`() = runTest {
        val maintenance = Maintenance(id = "123", assetId = "abc")
        every { getMaintenanceById("123") } returns flowOf(Resource.Success(maintenance))
        every { updateMaintenance(any()) } returns flowOf(Resource.Success(maintenance))

        val editViewModel = MaintenanceFormViewModel(
            addMaintenance, updateMaintenance, getMaintenanceById, parseDocument, scheduler,
            SavedStateHandle(mapOf("maintenanceId" to "123"))
        )
        advanceUntilIdle()

        editViewModel.onDateChange(1_700_000_000_000L)

        editViewModel.saveMaintenance()
        advanceUntilIdle()

        verify { updateMaintenance(any()) }
        verify(exactly = 0) { addMaintenance(any()) }
    }

    @Test
    fun `saveMaintenance schedules reminder on Success in edit mode`() = runTest {
        val maintenance = Maintenance(id = "123", assetId = "abc")
        every { getMaintenanceById("123") } returns flowOf(Resource.Success(maintenance))
        every { updateMaintenance(any()) } returns flowOf(Resource.Success(maintenance))

        val editViewModel = MaintenanceFormViewModel(
            addMaintenance, updateMaintenance, getMaintenanceById, parseDocument, scheduler,
            SavedStateHandle(mapOf("maintenanceId" to "123"))
        )
        advanceUntilIdle()

        editViewModel.onDateChange(1_700_000_000_000L)

        editViewModel.saveMaintenance()
        advanceUntilIdle()

        verify { scheduler.schedule(any(), any(), any(), any()) }
    }

    // ==================== loadMaintenance ====================

    @Test
    fun `loadMaintenance sets isLoading true during Loading`() = runTest {
        every { getMaintenanceById("123") } returns flowOf(Resource.Loading())

        val editViewModel = MaintenanceFormViewModel(
            addMaintenance, updateMaintenance, getMaintenanceById, parseDocument, scheduler,
            SavedStateHandle(mapOf("maintenanceId" to "123"))
        )
        advanceUntilIdle()

        assertEquals(true, editViewModel.state.value.isLoading)
    }

    @Test
    fun `loadMaintenance fills state after Success`() = runTest {
        val maintenance = Maintenance(
            id = "123", assetId = "abc", title = "Vidange",
            description = "Huile 5W30", type = MaintenanceType.RECURRING,
            date = 1_700_000_000_000L, cost = 150.0, provider = "Garage Auto",
            mileage = 50000, isCompleted = true, recurrenceMonths = 6
        )
        every { getMaintenanceById("123") } returns flowOf(Resource.Success(maintenance))

        val editViewModel = MaintenanceFormViewModel(
            addMaintenance, updateMaintenance, getMaintenanceById, parseDocument, scheduler,
            SavedStateHandle(mapOf("maintenanceId" to "123"))
        )
        advanceUntilIdle()

        assertEquals(false, editViewModel.state.value.isLoading)
        assertEquals("Vidange", editViewModel.state.value.title)
        assertEquals("Huile 5W30", editViewModel.state.value.description)
        assertEquals(MaintenanceType.RECURRING, editViewModel.state.value.type)
        assertEquals(1_700_000_000_000L, editViewModel.state.value.date)
        assertEquals("150.0", editViewModel.state.value.cost)
        assertEquals("Garage Auto", editViewModel.state.value.provider)
        assertEquals("50000", editViewModel.state.value.mileage)
        assertEquals(true, editViewModel.state.value.isCompleted)
        assertEquals("6", editViewModel.state.value.recurrenceMonths)
    }

    @Test
    fun `loadMaintenance sets error after Error`() = runTest {
        every { getMaintenanceById("123") } returns flowOf(Resource.Error("fetch error"))

        val editViewModel = MaintenanceFormViewModel(
            addMaintenance, updateMaintenance, getMaintenanceById, parseDocument, scheduler,
            SavedStateHandle(mapOf("maintenanceId" to "123"))
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
            maintenanceTitle = "Vidange",
            maintenanceDescription = "Huile moteur",
            maintenanceType = MaintenanceType.RECURRING,
            cost = "150",
            maintenanceProvider = "Garage Auto"
        )))

        val uri = mockk<Uri>()
        viewModel.onScanResult(uri)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals("Vidange", viewModel.state.value.title)
        assertEquals("Huile moteur", viewModel.state.value.description)
        assertEquals(MaintenanceType.RECURRING, viewModel.state.value.type)
        assertEquals("150", viewModel.state.value.cost)
        assertEquals("Garage Auto", viewModel.state.value.provider)
        assertEquals(true, viewModel.state.value.fromScanner)
    }

    @Test
    fun `onScanResult keeps existing values when scanned data is empty`() = runTest {
        viewModel.onTitleChange("Existing Title")
        viewModel.onProviderChange("Existing Provider")

        every { parseDocument(any()) } returns flowOf(Resource.Success(ScannedData(
            maintenanceTitle = "",
            maintenanceProvider = "",
            cost = ""
        )))

        val uri = mockk<Uri>()
        viewModel.onScanResult(uri)
        advanceUntilIdle()

        assertEquals("Existing Title", viewModel.state.value.title)
        assertEquals("Existing Provider", viewModel.state.value.provider)
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
    fun `init sets isEditing true and step INFO when maintenanceId is not empty`() = runTest {
        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        assertEquals(true, editViewModel.state.value.isEditing)
        assertEquals(MaintenanceFormStep.INFO, editViewModel.state.value.step)
    }

    @Test
    fun `init stays at SOURCE when maintenanceId is empty`() {
        assertEquals(false, viewModel.state.value.isEditing)
        assertEquals(MaintenanceFormStep.SOURCE, viewModel.state.value.step)
    }

    // ==================== Helpers ====================

    private fun mockSaveMaintenanceSuccess(): Maintenance {
        val maintenance = Maintenance(id = "newId", assetId = "abc")
        every { addMaintenance(any()) } returns flowOf(Resource.Success(maintenance))
        return maintenance
    }

    private fun createEditViewModel(maintenanceId: String = "123"): MaintenanceFormViewModel {
        val maintenance = Maintenance(id = maintenanceId, assetId = "abc")
        every { getMaintenanceById(maintenanceId) } returns flowOf(Resource.Success(maintenance))
        return MaintenanceFormViewModel(
            addMaintenance, updateMaintenance, getMaintenanceById, parseDocument, scheduler,
            SavedStateHandle(mapOf("maintenanceId" to maintenanceId))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }
}
