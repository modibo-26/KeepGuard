package com.modibo.keepguard.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.modibo.keepguard.presentation.screen.home.HomeScreen
import com.modibo.keepguard.presentation.screen.assets.detail.AssetDetailScreen
import com.modibo.keepguard.presentation.screen.assets.form.AssetFormScreen
import com.modibo.keepguard.presentation.screen.assets.list.AssetListScreen
import com.modibo.keepguard.presentation.screen.document.detail.DocumentDetailScreen
import com.modibo.keepguard.presentation.screen.document.form.DocumentFormScreen
import com.modibo.keepguard.presentation.screen.document.list.DocumentListScreen
import com.modibo.keepguard.presentation.screen.maintenance.detail.MaintenanceDetailScreen
import com.modibo.keepguard.presentation.screen.maintenance.form.MaintenanceFormScreen
import com.modibo.keepguard.presentation.screen.maintenance.list.MaintenanceListScreen
import com.modibo.keepguard.presentation.screen.scanner.ScannerScreen
import com.modibo.keepguard.presentation.screen.warranty.detail.WarrantyDetailScreen
import com.modibo.keepguard.presentation.screen.warranty.form.WarrantyFormScreen
import com.modibo.keepguard.presentation.screen.settings.SettingsScreen
import com.modibo.keepguard.presentation.screen.warranty.list.WarrantyListScreen

@Composable
fun NavGraph(navHostController: NavHostController) {
    NavHost(navHostController, Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAssets = {
                    navHostController.navigate(Screen.AssetList.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.AssetList.route) {
            AssetListScreen(
                onAssetClick = { assetId -> navHostController.navigate("asset_detail/$assetId") },
                onAddClick = { navHostController.navigate("asset_form") }
            )
        }
        composable(Screen.Scanner.route) {
            ScannerScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            "asset_detail/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            AssetDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { assetId -> navHostController.navigate("asset_form?assetId=$assetId") },
                onDelete = { navHostController.popBackStack() },
                onWarranties = { assetId -> navHostController.navigate("warranty_list/$assetId") },
                onMaintenances = { assetId -> navHostController.navigate("maintenance_list/$assetId") },
                onDocuments =  { assetId -> navHostController.navigate("document_list/$assetId") }
            )
        }
        composable(
            "asset_form?assetId={assetId}",
            arguments = listOf(navArgument("assetId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AssetFormScreen(
                onSaved = { navHostController.popBackStack() }
            )
        }
        composable(
            "warranty_list/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            WarrantyListScreen(
                onWarrantyClick = { warrantyId -> navHostController.navigate("warranty_detail/$warrantyId") },
                onAddClick = {
                    val assetId = it.arguments?.getString("assetId") ?: ""
                    navHostController.navigate("warranty_form/$assetId")
                },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            "warranty_detail/{warrantyId}",
            arguments = listOf(navArgument("warrantyId") { type = NavType.StringType })
        ) {
            WarrantyDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { warrantyId, assetId ->
                    navHostController.navigate("warranty_form/$assetId?warrantyId=$warrantyId")
                },
                onDelete = { navHostController.popBackStack() }
            )
        }
        composable(
            "warranty_form/{assetId}?warrantyId={warrantyId}",
            arguments = listOf(
                navArgument("assetId") { type = NavType.StringType },
                navArgument("warrantyId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            WarrantyFormScreen(
                onSaved = { navHostController.popBackStack() },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            "maintenance_list/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            MaintenanceListScreen(
                onMaintenanceClick = { maintenanceId -> navHostController.navigate("maintenance_detail/$maintenanceId") },
                onAddClick = {
                    val assetId = it.arguments?.getString("assetId") ?: ""
                    navHostController.navigate("maintenance_form/$assetId")
                },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            "maintenance_detail/{maintenanceId}",
            arguments = listOf(navArgument("maintenanceId") { type = NavType.StringType })
        ) {
            MaintenanceDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { maintenanceId, assetId ->
                    navHostController.navigate("maintenance_form/$assetId?maintenanceId=$maintenanceId")
                },
                onDelete = { navHostController.popBackStack() }
            )
        }
        composable(
            "maintenance_form/{assetId}?maintenanceId={maintenanceId}",
            arguments = listOf(
                navArgument("assetId") { type = NavType.StringType },
                navArgument("maintenanceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            MaintenanceFormScreen(
                onSaved = { navHostController.popBackStack() },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            "document_list/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            DocumentListScreen(
                onDocumentClick = { documentId -> navHostController.navigate("document_detail/$documentId") },
                onAddClick = {
                    val assetId = it.arguments?.getString("assetId") ?: ""
                    navHostController.navigate("document_form/$assetId")
                },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            "document_detail/{documentId}",
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) {
            DocumentDetailScreen(
                onBack = { navHostController.popBackStack() },
                onDelete = { navHostController.popBackStack() }
            )
        }
        composable(
            "document_form/{assetId}",
            arguments = listOf(
                navArgument("assetId") { type = NavType.StringType },
            )
        ) {
            DocumentFormScreen(
                onSaved = { navHostController.popBackStack() },
                onBack = { navHostController.popBackStack() }
            )
        }
    }
}