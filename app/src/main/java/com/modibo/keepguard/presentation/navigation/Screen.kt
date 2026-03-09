package com.modibo.keepguard.presentation.navigation

sealed class Screen (
    val route: String,
) {
    object Home: Screen("home")
    object AssetList : Screen("asset_list")
    object Scanner : Screen("scanner")
    object Settings : Screen("settings")
    object AssetDetail : Screen("asset_detail/{assetId}") {
        fun createRoute(assetId: String) = route.replace("{assetId}", assetId)
    }
    object AssetForm : Screen("asset_form?assetId={assetId}") {
        fun createRoute(assetId: String? = null) = route.replace("{assetId}", assetId.orEmpty())
    }
    object WarrantyList : Screen("warranty_list/{assetId}") {
        fun createRoute(assetId: String) = route.replace("{assetId}", assetId)
    }
    object WarrantyDetail : Screen("warranty_detail/{warrantyId}") {
        fun createRoute(warrantyId: String) = route.replace("{warrantyId}", warrantyId)
    }
    object WarrantyForm : Screen("warranty_form/{assetId}?warrantyId={warrantyId}") {
        fun createRoute(assetId: String? = null, warrantyId: String? = null) = route.replace("{assetId}", assetId.orEmpty()).replace("{warrantyId}", warrantyId.orEmpty())
    }
    object MaintenanceList : Screen("maintenance_list/{assetId}") {
        fun createRoute(assetId: String) = route.replace("{assetId}", assetId)
    }
    object MaintenanceDetail : Screen("maintenance_detail/{maintenanceId}") {
        fun createRoute(maintenanceId: String) = route.replace("{maintenanceId}", maintenanceId)
    }
    object MaintenanceForm : Screen("maintenance_form/{assetId}?maintenanceId={maintenanceId}") {
        fun createRoute(assetId: String, maintenanceId: String? = null) = route.replace("{assetId}", assetId).replace("{maintenanceId}", maintenanceId.orEmpty())
    }
    object DocumentList : Screen("document_list/{assetId}") {
        fun createRoute(assetId: String) = route.replace("{assetId}", assetId)
    }
    object DocumentDetail : Screen("document_detail/{documentId}") {
        fun createRoute(documentId: String) = route.replace("{documentId}", documentId)
    }
    object DocumentForm : Screen("document_form?assetId={assetId}&imageUri={imageUri}&ocrText={ocrText}") {
        fun createRoute(assetId: String? = null, imageUri: String? = null, ocrText: String? = null): String {
            return "document_form" +
                    "?assetId=${assetId.orEmpty()}" +
                    "&imageUri=${imageUri.orEmpty()}" +
                    "&ocrText=${ocrText.orEmpty()}"
        }
    }
}