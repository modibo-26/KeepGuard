package com.modibo.keepguard.presentation.screen.maintenance.list

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenancesByAssetUseCase
import io.mockk.every
import io.mockk.mockk
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
class MaintenanceListViewModelTest {

    private lateinit var getMaintenancesByAsset: GetMaintenancesByAssetUseCase
    private lateinit var viewModel: MaintenanceListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getMaintenancesByAsset = mockk()
        every { getMaintenancesByAsset(any()) } returns flowOf(Resource.Success(emptyList()))
        viewModel = MaintenanceListViewModel(getMaintenancesByAsset, SavedStateHandle(mapOf("assetId" to "abc")))
    }

    // ==================== loadMaintenances ====================

    @Test
    fun `loadMaintenances sets isLoading true during Loading`() = runTest {
        every { getMaintenancesByAsset(any()) } returns flowOf(Resource.Loading())
        viewModel.loadMaintenances()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadMaintenances fills maintenances after Success`() = runTest {
        val maintenances = listOf(Maintenance(id = "1"), Maintenance(id = "2"))
        every { getMaintenancesByAsset(any()) } returns flowOf(Resource.Success(maintenances))
        viewModel.loadMaintenances()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.maintenances.size)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadMaintenances sets error after Error`() = runTest {
        every { getMaintenancesByAsset(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadMaintenances()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
