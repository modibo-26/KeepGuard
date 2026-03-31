package com.modibo.keepguard.presentation.screen.assets.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.Asset
import com.modibo.keepguard.domain.model.AssetCategory
import com.modibo.keepguard.domain.model.AssetCondition
import com.modibo.keepguard.domain.model.AssetSubCategory
import com.modibo.keepguard.domain.model.PurchaseMode
import com.modibo.keepguard.domain.model.ScannedData
import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.model.WarrantyType
import com.modibo.keepguard.domain.usecase.asset.AddAssetUseCase
import com.modibo.keepguard.domain.usecase.asset.GetAssetByIdUseCase
import com.modibo.keepguard.domain.usecase.asset.UpdateAssetUseCase
import com.modibo.keepguard.domain.usecase.scanner.ParseDocumentUseCase
import com.modibo.keepguard.domain.usecase.warranty.AddWarrantyUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssetFormViewModelTest {
    // Mocks des use cases
    private lateinit var addAsset: AddAssetUseCase
    private lateinit var getAssetById: GetAssetByIdUseCase
    private lateinit var updateAsset: UpdateAssetUseCase
    private lateinit var parseDocument: ParseDocumentUseCase
    private lateinit var addWarranty: AddWarrantyUseCase

    private lateinit var viewModel: AssetFormViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        addAsset = mockk()
        getAssetById = mockk()
        updateAsset = mockk()
        parseDocument = mockk()
        addWarranty = mockk()

        mockkStatic(Uri::class)
        every { Uri.decode(any()) } returns ""
        every { Uri.encode(any()) } returns ""

        viewModel = AssetFormViewModel(
            addAsset,
            getAssetById,
            updateAsset,
            parseDocument,
            addWarranty,
            SavedStateHandle()
        )
    }

    // ==================== Navigation ====================

    @Test
    fun `nextStep from PHOTO goes to CATEGORY`() {
        viewModel.nextStep()
        assertEquals(AssetFormStep.CATEGORY, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `nextStep from CATEGORY goes to INFO_PURCHASE`() {
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(AssetFormStep.INFO_PURCHASE, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `nextStep from INFO_PURCHASE goes to RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(AssetFormStep.RECAP, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `nextStep from RECAP stays RECAP`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(AssetFormStep.RECAP, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `prevStep from CATEGORY goes to PHOTO`() {
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(AssetFormStep.PHOTO, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `prevStep from INFO_PURCHASE goes to CATEGORY`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(AssetFormStep.CATEGORY, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `prevStep from RECAP goes to INFO_PURCHASE`() {
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.prevStep()
        assertEquals(AssetFormStep.INFO_PURCHASE, viewModel.state.value.assetFormStep)
    }

    @Test
    fun `prevStep from PHOTO stays PHOTO`() {
        viewModel.prevStep()
        assertEquals(AssetFormStep.PHOTO, viewModel.state.value.assetFormStep)
    }

    // ==================== Champs simples ====================

    @Test
    fun `onNameChange updates name`() {
        viewModel.onNameChange("test")
        assertEquals("test", viewModel.state.value.name)
    }

    @Test
    fun `onCategoryChange updates category`() {
        viewModel.onCategoryChange(AssetCategory.FURNITURE)
        assertEquals(AssetCategory.FURNITURE, viewModel.state.value.category)
    }

    @Test
    fun `onPurchasePriceChange updates purchasePrice`() {
        viewModel.onPurchasePriceChange(12.00)
        assertEquals(12.00, viewModel.state.value.purchasePrice)
    }

    @Test
    fun `onBrandChange updates brand`() {
        viewModel.onBrandChange("test")
        assertEquals("test", viewModel.state.value.brand)
    }

    @Test
    fun `onModelChange updates model`() {
        viewModel.onModelChange("test")
        assertEquals("test", viewModel.state.value.model)
    }

    @Test
    fun `onDescriptionChange updates description`() {
        viewModel.onDescriptionChange("test")
        assertEquals("test", viewModel.state.value.description)
    }

    @Test
    fun `onSubCategoryChange updates subCategory`() {
        viewModel.onSubCategoryChange(AssetSubCategory.KITCHEN_FURNITURE)
        assertEquals(AssetSubCategory.KITCHEN_FURNITURE, viewModel.state.value.subCategory)
    }

    @Test
    fun `onPurchaseModeChange updates purchaseMode`() {
        viewModel.onPurchaseModeChange(PurchaseMode.IN_STORE)
        assertEquals(PurchaseMode.IN_STORE, viewModel.state.value.purchaseMode)
    }

    @Test
    fun `onPurchaseDateChange updates purchaseDate`() {
        viewModel.onPurchaseDateChange(1_700_000_000_000L)
        assertEquals(1_700_000_000_000L, viewModel.state.value.purchaseDate)
    }

    @Test
    fun `onSerialNumberChange updates serialNumber`() {
        viewModel.onSerialNumberChange("test")
        assertEquals("test", viewModel.state.value.serialNumber)
    }

    @Test
    fun `onPurchasePlaceChange updates purchasePlace`() {
        viewModel.onPurchasePlaceChange("test")
        assertEquals("test", viewModel.state.value.purchasePlace)
    }

    @Test
    fun `onConditionChange updates condition`() {
        viewModel.onConditionChange(AssetCondition.RECONDITIONED)
        assertEquals(AssetCondition.RECONDITIONED, viewModel.state.value.condition)
    }

    @Test
    fun `onWarrantyMonthsChange updates warrantyMonths`() {
        viewModel.onWarrantyMonthsChange(12)
        assertEquals(12, viewModel.state.value.warrantyMonths)
    }

    @Test
    fun `onImageUriChange updates imageUri`() {
        val uri = mockk<Uri>()
        viewModel.onImageUriChange(uri)
        assertEquals(uri, viewModel.state.value.imageUri)
    }

    // ==================== saveAsset — création ====================

    @Test
    fun `saveAsset calls addAsset when assetId is empty`() = runTest {
        mockSaveAssetSuccess()

        viewModel.saveAsset()
        advanceUntilIdle()

        verify { addAsset(any(), any()) }
        verify(exactly = 0) { updateAsset(any(), any()) }

    }

    @Test
    fun `saveAsset sets isSaved true after Success`() = runTest {
        mockSaveAssetSuccess()

        viewModel.saveAsset()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isSaved)
        assertEquals(false, viewModel.state.value.isLoading)

    }

    @Test
    fun `saveAsset sets isLoading true during Loading`() = runTest {
        every { addAsset(any(), any()) } returns flowOf(Resource.Loading())

        viewModel.saveAsset()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveAsset sets error after Error`() = runTest {
        every { addAsset(any(), any()) } returns flowOf(Resource.Error("erreur test"))

        viewModel.saveAsset()
        advanceUntilIdle()

        assertEquals("erreur test", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `saveAsset creates legal warranty 24 months when NEW`() = runTest {
        mockSaveAssetSuccess()

        viewModel.saveAsset()
        advanceUntilIdle()

        verify { addWarranty(match { it.type == WarrantyType.LEGAL && it.durationMonths == 24 }) }
    }

    @Test
    fun `saveAsset creates legal warranty 12 months when RECONDITIONED`() = runTest {
        mockSaveAssetSuccess()

        viewModel.onConditionChange(AssetCondition.RECONDITIONED)
        viewModel.saveAsset()
        advanceUntilIdle()

        verify { addWarranty(match { it.type == WarrantyType.LEGAL && it.durationMonths == 12 }) }
    }

    @Test
    fun `saveAsset no legal warranty when USED`() = runTest {
        mockSaveAssetSuccess()

        viewModel.onConditionChange(AssetCondition.USED)
        viewModel.saveAsset()
        advanceUntilIdle()

        verify(exactly = 0) { addWarranty(match { it.type == WarrantyType.LEGAL }) }

    }

    @Test
    fun `saveAsset creates manufacturer warranty when warrantyMonths greater than 0`() = runTest {
        mockSaveAssetSuccess()

        viewModel.onWarrantyMonthsChange(2)
        viewModel.saveAsset()
        advanceUntilIdle()

        verify { addWarranty(match { it.type == WarrantyType.MANUFACTURER && it.durationMonths == 2 }) }
    }

    @Test
    fun `saveAsset creates retractation warranty when ONLINE`() = runTest {
        mockSaveAssetSuccess()

        viewModel.onPurchaseModeChange(PurchaseMode.ONLINE)
        viewModel.saveAsset()
        advanceUntilIdle()

        verify { addWarranty(match { it.type == WarrantyType.RETRACTATION }) }
    }

    @Test
    fun `saveAsset no warranty when no conditions met`() = runTest {
        mockSaveAssetSuccess()
        viewModel.onConditionChange(AssetCondition.USED)

        viewModel.saveAsset()
        advanceUntilIdle()

        verify(exactly = 0) { addWarranty(any()) }
    }

    // ==================== saveAsset — édition ====================

    @Test
    fun `saveAsset calls updateAsset when assetId is not empty`() = runTest {
        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        editViewModel.saveAsset()
        advanceUntilIdle()

        verify { updateAsset(any(), any()) }
        verify(exactly = 0) { addAsset(any(), any()) }
    }

    @Test
    fun `saveAsset no warranties created in edit mode`() = runTest {
        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        editViewModel.saveAsset()
        advanceUntilIdle()

        verify(exactly = 0) { addWarranty(any()) }
    }
    // ==================== loadAsset ====================

    @Test
    fun `loadAsset sets isLoading true during Loading`() = runTest {
        every { getAssetById("123") } returns flowOf(Resource.Loading())

        val editViewModel = AssetFormViewModel(
            addAsset, getAssetById, updateAsset, parseDocument, addWarranty,
            SavedStateHandle(mapOf("assetId" to "123"))
        )
        advanceUntilIdle()
        assertEquals(true, editViewModel.state.value.isLoading)
    }

    @Test
    fun `loadAsset fills state after Success`() = runTest {
        val asset = Asset(id = "123", name = "TV", brand = "Samsung")
        every { getAssetById("123") } returns flowOf(Resource.Success(asset))
        val editViewModel = AssetFormViewModel(
            addAsset, getAssetById, updateAsset, parseDocument, addWarranty,
            SavedStateHandle(mapOf("assetId" to "123"))
        )
        advanceUntilIdle()
        assertEquals(false, editViewModel.state.value.isLoading)
        assertEquals("TV", editViewModel.state.value.name)
        assertEquals("Samsung", editViewModel.state.value.brand)
    }

    @Test
    fun `loadAsset sets error after Error`() = runTest {
        every { getAssetById("123") } returns flowOf(Resource.Error("fetch error"))
        val editViewModel = AssetFormViewModel(
            addAsset, getAssetById, updateAsset, parseDocument, addWarranty,
            SavedStateHandle(mapOf("assetId" to "123"))
        )
        advanceUntilIdle()
        assertEquals(false, editViewModel.state.value.isLoading)
        assertEquals("fetch error", editViewModel.state.value.error)
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
    fun `onImageCaptured updates state with scanned data`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Success(ScannedData(brand = "Samsung", model = "TV", name = "Smart TV")))

        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals("Samsung", viewModel.state.value.brand)
        assertEquals("TV", viewModel.state.value.model)
    }

    @Test
    fun `onImageCaptured sets warrantyMonths null when data is 0`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Success(ScannedData(
            warrantyMonths = 0
        )))

        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.warrantyMonths)
    }

    @Test
    fun `onImageCaptured sets error after Error`() = runTest {
        every { parseDocument(any()) } returns flowOf(Resource.Error("error capture"))

        val uri = mockk<Uri>()
        viewModel.onImageCaptured(uri)
        advanceUntilIdle()

        assertEquals("error capture", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== Init ====================

    @Test
    fun `init prefills fields from scannedJson`() = runTest {
        every { Uri.decode(any()) } answers { firstArg() }

        val scannedData = ScannedData(
            name = "Smart TV",
            brand = "Samsung",
            model = "UE55",
            serialNumber = "SN123",
            purchasePlace = "Amazon",
            purchasePrice = "599.99",
            warrantyMonths = 24
        )
        val jsonString = Json.encodeToString(ScannedData.serializer(), scannedData)
        val editViewModel = AssetFormViewModel(
            addAsset, getAssetById, updateAsset, parseDocument, addWarranty,
            SavedStateHandle(mapOf(
                "scannedJson" to jsonString,
                "imageUri" to "fakeUri"
            ))
        )

        assertEquals("Smart TV", editViewModel.state.value.name)
        assertEquals("Samsung", editViewModel.state.value.brand)
        assertEquals("UE55", editViewModel.state.value.model)
        assertEquals("SN123", editViewModel.state.value.serialNumber)
        assertEquals("Amazon", editViewModel.state.value.purchasePlace)
        assertEquals(599.99, editViewModel.state.value.purchasePrice)
        assertEquals(24, editViewModel.state.value.warrantyMonths)

    }

    @Test
    fun `init sets isEditing true when assetId is not empty`() = runTest {

        val editViewModel = createEditViewModel()
        advanceUntilIdle()

        assertEquals(true, editViewModel.state.value.isEditing)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }


    private fun mockSaveAssetSuccess(): Asset {
        val asset = Asset(id = "newId")
        every { addAsset(any(), any()) } returns flowOf(Resource.Success(asset))
        every { addWarranty(any()) } returns flowOf(Resource.Success(Warranty()))
        return asset
    }

    private fun createEditViewModel(assetId: String = "123"): AssetFormViewModel {
        val asset = Asset(id = assetId)
        every { getAssetById(assetId) } returns flowOf(Resource.Success(asset))
        every { updateAsset(any(), any()) } returns flowOf(Resource.Success(asset))
        return AssetFormViewModel(
            addAsset, getAssetById, updateAsset, parseDocument, addWarranty,
            SavedStateHandle(mapOf("assetId" to assetId))
        )
    }
}
