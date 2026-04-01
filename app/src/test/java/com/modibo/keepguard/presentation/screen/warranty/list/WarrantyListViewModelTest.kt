package com.modibo.keepguard.presentation.screen.warranty.list

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.usecase.warranty.GetWarrantiesByAssetUseCase
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
class WarrantyListViewModelTest {

    private lateinit var getWarrantiesByAsset: GetWarrantiesByAssetUseCase
    private lateinit var viewModel: WarrantyListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getWarrantiesByAsset = mockk()
        every { getWarrantiesByAsset(any()) } returns flowOf(Resource.Success(emptyList()))
        viewModel = WarrantyListViewModel(getWarrantiesByAsset, SavedStateHandle(mapOf("assetId" to "abc")))
    }

    // ==================== loadWarranties ====================

    @Test
    fun `loadWarranties sets isLoading true during Loading`() = runTest {
        every { getWarrantiesByAsset(any()) } returns flowOf(Resource.Loading())
        viewModel.loadWarranties()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadWarranties fills warranties after Success`() = runTest {
        val warranties = listOf(Warranty(id = "1"), Warranty(id = "2"))
        every { getWarrantiesByAsset(any()) } returns flowOf(Resource.Success(warranties))
        viewModel.loadWarranties()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.warranties.size)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadWarranties sets error after Error`() = runTest {
        every { getWarrantiesByAsset(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadWarranties()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
