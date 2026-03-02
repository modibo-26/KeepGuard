package com.modibo.keepguard.presentation.navigation

sealed class Screen (
    val route: String,
) {
    object Home: Screen("home")
    object AssetList : Screen("asset_list")
    object Scanner : Screen("scanner")
    object Settings : Screen("settings")
    data class AssetDetail(val assetId : String) : Screen("asset_detail/{assetId}")
    data class AssetForm(val assetId : String? = null) : Screen("asset_form?assetId={assetId}")
    data class WarrantyList(val assetId: String) : Screen("warranty_list/{assetId}")
    data class WarrantyDetail(val warrantyId: String) : Screen("warranty_detail/{warrantyId}")
    data class WarrantyForm(val assetId: String, val warrantyId: String? = null) : Screen("warranty_form/{assetId}?warrantyId={warrantyId}")
    data class MaintenanceForm(val assetId : String) : Screen("maintenance_form/{assetId}")
}