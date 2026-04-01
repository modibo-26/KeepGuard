package com.modibo.keepguard.presentation.screen.document.detail

import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Document
import com.modibo.keepguard.domain.usecase.document.DeleteDocumentUseCase
import com.modibo.keepguard.domain.usecase.document.GetDocumentByIdUseCase
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
class DocumentDetailViewModelTest {

    private lateinit var getDocumentById: GetDocumentByIdUseCase
    private lateinit var deleteDocument: DeleteDocumentUseCase
    private lateinit var viewModel: DocumentDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        getDocumentById = mockk()
        deleteDocument = mockk()
        every { getDocumentById(any()) } returns flowOf(Resource.Success(Document(id = "abc")))
        viewModel = DocumentDetailViewModel(getDocumentById, deleteDocument, SavedStateHandle(mapOf("documentId" to "abc")))
    }

    // ==================== loadDocument ====================

    @Test
    fun `loadDocument sets isLoading true during Loading`() = runTest {
        every { getDocumentById(any()) } returns flowOf(Resource.Loading())
        viewModel.loadDocument()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadDocument fills document after Success`() = runTest {
        val document = Document(id = "abc", name = "Facture", merchant = "Amazon")
        every { getDocumentById(any()) } returns flowOf(Resource.Success(document))
        viewModel.loadDocument()
        advanceUntilIdle()
        assertEquals("Facture", viewModel.state.value.document?.name)
        assertEquals("Amazon", viewModel.state.value.document?.merchant)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `loadDocument sets error after Error`() = runTest {
        every { getDocumentById(any()) } returns flowOf(Resource.Error("fetch error"))
        viewModel.loadDocument()
        advanceUntilIdle()
        assertEquals("fetch error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== deleteDocument ====================

    @Test
    fun `deleteDocument sets isDeleted true after Success`() = runTest {
        every { deleteDocument(any()) } returns flowOf(Resource.Success(Unit))
        viewModel.onDeleteDocument()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isDeleted)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteDocument sets isLoading true during Loading`() = runTest {
        every { deleteDocument(any()) } returns flowOf(Resource.Loading())
        viewModel.onDeleteDocument()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `deleteDocument sets error after Error`() = runTest {
        every { deleteDocument(any()) } returns flowOf(Resource.Error("delete error"))
        viewModel.onDeleteDocument()
        advanceUntilIdle()
        assertEquals("delete error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
