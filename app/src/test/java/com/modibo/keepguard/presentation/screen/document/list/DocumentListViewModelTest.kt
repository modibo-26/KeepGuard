package com.modibo.keepguard.presentation.screen.document.list

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.usecase.document.GetDocumentsByAssetUseCase
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
class DocumentListViewModelTest {

    private lateinit var getDocumentsByAsset: GetDocumentsByAssetUseCase
    private lateinit var viewModel: DocumentListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getDocumentsByAsset = mockk()
        every { getDocumentsByAsset(any()) } returns flowOf(Resource.Success(emptyList()))
        viewModel = DocumentListViewModel(getDocumentsByAsset, SavedStateHandle(mapOf("assetId" to "abc")))
    }

    // ==================== loadDocuments ====================

    @Test
    fun `loadDocuments sets isLoading true during Loading`() = runTest {
        every { getDocumentsByAsset(any()) } returns flowOf(Resource.Loading())
        viewModel.loadDocuments()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadDocuments fills documents after Success`() = runTest {
        val documents = listOf(Document(id = "1"), Document(id = "2"))
        every { getDocumentsByAsset(any()) } returns flowOf(Resource.Success(documents))
        viewModel.loadDocuments()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.documents.size)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadDocuments sets error after Error`() = runTest {
        every { getDocumentsByAsset(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadDocuments()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
