package com.modibo.keepguard.presentation.screen.maintenance.detail

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.worker.ReminderScheduler
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByIdUseCase
import io.mockk.every
import io.mockk.mockk
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
class MaintenanceDetailViewModelTest {

    private lateinit var getMaintenanceById: GetMaintenanceByIdUseCase
    private lateinit var deleteMaintenance: DeleteMaintenanceUseCase
    private lateinit var scheduler: ReminderScheduler
    private lateinit var viewModel: MaintenanceDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getMaintenanceById = mockk()
        deleteMaintenance = mockk()
        scheduler = mockk(relaxed = true)
        every { getMaintenanceById(any()) } returns flowOf(Resource.Success(Maintenance(id = "abc")))
        viewModel = MaintenanceDetailViewModel(getMaintenanceById, deleteMaintenance, scheduler, SavedStateHandle(mapOf("maintenanceId" to "abc")))
    }

    // ==================== loadMaintenance ====================

    @Test
    fun `loadMaintenance sets isLoading true during Loading`() = runTest {
        every { getMaintenanceById(any()) } returns flowOf(Resource.Loading())
        viewModel.loadMaintenance()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadMaintenance fills maintenance after Success`() = runTest {
        val maintenance = Maintenance(id = "abc", title = "Vidange", provider = "Garage Auto")
        every { getMaintenanceById(any()) } returns flowOf(Resource.Success(maintenance))
        viewModel.loadMaintenance()
        advanceUntilIdle()
        assertEquals("Vidange", viewModel.state.value.maintenance?.title)
        assertEquals("Garage Auto", viewModel.state.value.maintenance?.provider)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadMaintenance sets error after Error`() = runTest {
        every { getMaintenanceById(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadMaintenance()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== deleteMaintenance ====================

    @Test
    fun `deleteMaintenance sets isDeleted true after Success`() = runTest {
        every { deleteMaintenance(any()) } returns flowOf(Resource.Success(Unit))
        viewModel.deleteMaintenance()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isDeleted)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteMaintenance cancels reminder on Success`() = runTest {
        every { deleteMaintenance(any()) } returns flowOf(Resource.Success(Unit))
        viewModel.deleteMaintenance()
        advanceUntilIdle()
        verify { scheduler.cancel("abc") }
    }

    @Test
    fun `deleteMaintenance sets isLoading true during Loading`() = runTest {
        every { deleteMaintenance(any()) } returns flowOf(Resource.Loading())
        viewModel.deleteMaintenance()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteMaintenance sets error after Error`() = runTest {
        every { deleteMaintenance(any()) } returns flowOf(Resource.Error("delete error"))
        viewModel.deleteMaintenance()
        advanceUntilIdle()
        assertEquals("delete error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
