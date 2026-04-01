package com.modibo.keepguard.presentation.screen.warranty.detail

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.data.worker.ReminderScheduler
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.usecase.warranty.DeleteWarrantyUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantyByIdUseCase
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
class WarrantyDetailViewModelTest {

    private lateinit var getWarrantyById: GetWarrantyByIdUseCase
    private lateinit var deleteWarranty: DeleteWarrantyUseCase
    private lateinit var scheduler: ReminderScheduler
    private lateinit var viewModel: WarrantyDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getWarrantyById = mockk()
        deleteWarranty = mockk()
        scheduler = mockk(relaxed = true)
        every { getWarrantyById(any()) } returns flowOf(Resource.Success(Warranty(id = "abc")))
        viewModel = WarrantyDetailViewModel(getWarrantyById, deleteWarranty, scheduler, SavedStateHandle(mapOf("warrantyId" to "abc")))
    }

    // ==================== loadWarranty ====================

    @Test
    fun `loadWarranty sets isLoading true during Loading`() = runTest {
        every { getWarrantyById(any()) } returns flowOf(Resource.Loading())
        viewModel.loadWarranty()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadWarranty fills warranty after Success`() = runTest {
        val warranty = Warranty(id = "abc", provider = "Samsung", durationMonths = 24)
        every { getWarrantyById(any()) } returns flowOf(Resource.Success(warranty))
        viewModel.loadWarranty()
        advanceUntilIdle()
        assertEquals("Samsung", viewModel.state.value.warranty?.provider)
        assertEquals(24, viewModel.state.value.warranty?.durationMonths)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadWarranty sets error after Error`() = runTest {
        every { getWarrantyById(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadWarranty()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== deleteWarranty ====================

    @Test
    fun `deleteWarranty sets isDeleted true after Success`() = runTest {
        every { deleteWarranty(any()) } returns flowOf(Resource.Success(Unit))
        viewModel.deleteWarranty()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isDeleted)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteWarranty cancels reminder on Success`() = runTest {
        every { deleteWarranty(any()) } returns flowOf(Resource.Success(Unit))
        viewModel.deleteWarranty()
        advanceUntilIdle()
        verify { scheduler.cancel("abc") }
    }

    @Test
    fun `deleteWarranty sets isLoading true during Loading`() = runTest {
        every { deleteWarranty(any()) } returns flowOf(Resource.Loading())
        viewModel.deleteWarranty()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteWarranty sets error after Error`() = runTest {
        every { deleteWarranty(any()) } returns flowOf(Resource.Error("delete error"))
        viewModel.deleteWarranty()
        advanceUntilIdle()
        assertEquals("delete error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
