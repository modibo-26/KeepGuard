package com.modibo.keepguard.presentation.screen.assets.detail

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.usecase.asset.DeleteAssetUseCase
import com.modibo.keepguard.domain.usecase.asset.GetAssetByIdUseCase
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
class AssetDetailViewModelTest {

    private lateinit var getAssetById: GetAssetByIdUseCase
    private lateinit var deleteAsset: DeleteAssetUseCase
    private lateinit var viewModel: AssetDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getAssetById = mockk()
        deleteAsset = mockk()
        every { getAssetById(any()) } returns flowOf(Resource.Success(Asset(id = "abc")))
        viewModel = AssetDetailViewModel(getAssetById, deleteAsset, SavedStateHandle(mapOf("assetId" to "abc")))
    }

    // ==================== loadAsset ====================

    @Test
    fun `loadAsset sets isLoading true during Loading`() = runTest {
        every { getAssetById(any()) } returns flowOf(Resource.Loading())
        viewModel.loadAsset()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadAsset fills asset after Success`() = runTest {
        val asset = Asset(id = "abc", name = "TV", brand = "Samsung")
        every { getAssetById(any()) } returns flowOf(Resource.Success(asset))
        viewModel.loadAsset()
        advanceUntilIdle()
        assertEquals("TV", viewModel.state.value.asset?.name)
        assertEquals("Samsung", viewModel.state.value.asset?.brand)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadAsset sets error after Error`() = runTest {
        every { getAssetById(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadAsset()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== deleteAsset ====================

    @Test
    fun `deleteAsset sets isDeleted true after Success`() = runTest {
        every { deleteAsset(any()) } returns flowOf(Resource.Success(Unit))
        viewModel.deleteAsset()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isDeleted)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteAsset sets isLoading true during Loading`() = runTest {
        every { deleteAsset(any()) } returns flowOf(Resource.Loading())
        viewModel.deleteAsset()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteAsset sets error after Error`() = runTest {
        every { deleteAsset(any()) } returns flowOf(Resource.Error("delete error"))
        viewModel.deleteAsset()
        advanceUntilIdle()
        assertEquals("delete error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
