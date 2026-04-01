package com.modibo.keepguard.presentation.screen.assets.list

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.usecase.asset.GetAssetsUseCase
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
class AssetListViewModelTest {

    private lateinit var getAssets: GetAssetsUseCase
    private lateinit var viewModel: AssetListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getAssets = mockk()
        every { getAssets() } returns flowOf(Resource.Success(emptyList()))
        viewModel = AssetListViewModel(getAssets)
    }

    // ==================== loadAssets ====================

    @Test
    fun `loadAssets sets isLoading true during Loading`() = runTest {
        every { getAssets() } returns flowOf(Resource.Loading())
        viewModel.loadAssets()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadAssets fills assets after Success`() = runTest {
        val assets = listOf(Asset(id = "1", name = "TV"), Asset(id = "2", name = "Frigo"))
        every { getAssets() } returns flowOf(Resource.Success(assets))
        viewModel.loadAssets()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.assets.size)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadAssets sets error after Error`() = runTest {
        every { getAssets() } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadAssets()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
