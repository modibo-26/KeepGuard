package com.modibo.keepguard.presentation.screen.home

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.Maintenance
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase
import com.modibo.keepguard.domain.usecase.maintenance.GetMaintenanceByUserUseCase
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantiesByUserUseCase
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
class HomeViewModelTest {

    private lateinit var getAssets: GetAssetsUseCase
    private lateinit var getWarrantiesByUser: GetWarrantiesByUserUseCase
    private lateinit var getMaintenanceByUser: GetMaintenanceByUserUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getAssets = mockk()
        getWarrantiesByUser = mockk()
        getMaintenanceByUser = mockk()
        every { getAssets() } returns flowOf(Resource.Success(emptyList()))
        every { getWarrantiesByUser() } returns flowOf(Resource.Success(emptyList()))
        every { getMaintenanceByUser() } returns flowOf(Resource.Success(emptyList()))
        viewModel = HomeViewModel(getAssets, getWarrantiesByUser, getMaintenanceByUser)
    }

    // ==================== loadData ====================

    @Test
    fun `loadData fills assets after Success`() = runTest {
        val assets = listOf(Asset(id = "1"), Asset(id = "2"))
        every { getAssets() } returns flowOf(Resource.Success(assets))
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.assets.size)
    }

    @Test
    fun `loadData fills warranties after Success`() = runTest {
        val warranties = listOf(Warranty(id = "1"), Warranty(id = "2"))
        every { getWarrantiesByUser() } returns flowOf(Resource.Success(warranties))
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.warranties.size)
    }

    @Test
    fun `loadData fills maintenances after Success`() = runTest {
        val maintenances = listOf(Maintenance(id = "1"), Maintenance(id = "2"))
        every { getMaintenanceByUser() } returns flowOf(Resource.Success(maintenances))
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.maintenances.size)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadData sets error when assets fail`() = runTest {
        every { getAssets() } returns flowOf(Resource.Error("assets error"))
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals("assets error", viewModel.state.value.error)
    }

    @Test
    fun `loadData sets isLoading true at start`() = runTest {
        every { getMaintenanceByUser() } returns flowOf(Resource.Loading())
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    // ==================== Computed properties ====================

    @Test
    fun `assetCount returns correct count`() = runTest {
        val assets = listOf(Asset(id = "1"), Asset(id = "2"), Asset(id = "3"))
        every { getAssets() } returns flowOf(Resource.Success(assets))
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals(3, viewModel.state.value.assetCount)
    }

    @Test
    fun `pendingMaintenances returns only incomplete`() = runTest {
        val maintenances = listOf(
            Maintenance(id = "1", isCompleted = false),
            Maintenance(id = "2", isCompleted = true),
            Maintenance(id = "3", isCompleted = false)
        )
        every { getMaintenanceByUser() } returns flowOf(Resource.Success(maintenances))
        viewModel.loadData()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.pendingMaintenances.size)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
