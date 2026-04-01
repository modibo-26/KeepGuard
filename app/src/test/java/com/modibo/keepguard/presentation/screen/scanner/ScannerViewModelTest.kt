package com.modibo.keepguard.presentation.screen.scanner

import android.net.Uri
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
class ScannerViewModelTest {

    private lateinit var parseDocument: ParseDocumentUseCase
    private lateinit var viewModel: ScannerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        parseDocument = mockk()
        mockkStatic(Uri::class)
        viewModel = ScannerViewModel(parseDocument)
    }

    // ==================== onImageCaptured ====================

    @Test
    fun `onImageCaptured sets isLoading true during Loading`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Loading())
        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `onImageCaptured fills scanned data after Success`() = runTest {
        val data = ScannedData(name = "Facture", brand = "Samsung")
        every { parseDocument(any()) } returns flowOf(Resource.Success(data))
        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()
        assertEquals("Facture", viewModel.state.value.scanned?.name)
        assertEquals("Samsung", viewModel.state.value.scanned?.brand)
        assertEquals(uri, viewModel.state.value.capturedImageUri)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `onImageCaptured sets error after Error`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Error("scan error"))
        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()
        assertEquals("scan error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== resetScan ====================

    @Test
    fun `resetScan clears scanned data and uri`() = runTest {
        val data = ScannedData(name = "Facture")
        every { parseDocument(any()) } returns flowOf(Resource.Success(data))
        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()

        viewModel.resetScan()

        assertEquals(null, viewModel.state.value.scanned)
        assertEquals(null, viewModel.state.value.capturedImageUri)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }
}
