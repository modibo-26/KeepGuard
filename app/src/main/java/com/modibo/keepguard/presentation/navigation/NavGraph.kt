package com.modibo.keepguard.presentation.navigation

import android.net.Uri
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
                onAssetClick = { assetId -> navHostController.navigate(Screen.AssetDetail.createRoute(assetId)) },
                onAddClick = { navHostController.navigate(Screen.AssetForm.createRoute()) }
            )
        }
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onCreateDocument = { uri, ocr ->
                    navHostController.navigate(Screen.DocumentForm.createRoute(null,  Uri.encode(uri.toString()), Uri.encode(ocr)))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            Screen.AssetDetail.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            AssetDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { assetId -> navHostController.navigate(Screen.AssetForm.createRoute(assetId)) },
                onDelete = { navHostController.popBackStack() },
                onWarranties = { assetId -> navHostController.navigate(Screen.WarrantyList.createRoute(assetId)) },
                onMaintenances = { assetId -> navHostController.navigate(Screen.MaintenanceList.createRoute(assetId)) },
                onDocuments =  { assetId -> navHostController.navigate(Screen.DocumentList.createRoute(assetId)) }
            )
        }
        composable(
            Screen.AssetForm.route,
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
            Screen.WarrantyList.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            WarrantyListScreen(
                onWarrantyClick = { warrantyId -> navHostController.navigate(Screen.WarrantyDetail.createRoute(warrantyId)) },
                onAddClick = {
                    val assetId = it.arguments?.getString("assetId") ?: ""
                    navHostController.navigate(Screen.WarrantyForm.createRoute(assetId))
                },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            Screen.WarrantyDetail.route,
            arguments = listOf(navArgument("warrantyId") { type = NavType.StringType })
        ) {
            WarrantyDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { assetId, warrantyId ->
                    navHostController.navigate(Screen.WarrantyForm.createRoute(assetId, warrantyId))
                },
                onDelete = { navHostController.popBackStack() }
            )
        }
        composable(
            Screen.WarrantyForm.route,
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
            Screen.MaintenanceList.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            MaintenanceListScreen(
                onMaintenanceClick = { maintenanceId -> navHostController.navigate(Screen.MaintenanceDetail.createRoute(maintenanceId)) },
                onAddClick = {
                    val assetId = it.arguments?.getString("assetId") ?: ""
                    navHostController.navigate(Screen.MaintenanceForm.createRoute(assetId))
                },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            Screen.MaintenanceDetail.route,
            arguments = listOf(navArgument("maintenanceId") { type = NavType.StringType })
        ) {
            MaintenanceDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { assetId, maintenanceId ->
                    navHostController.navigate(Screen.MaintenanceForm.createRoute(assetId, maintenanceId))
                },
                onDelete = { navHostController.popBackStack() }
            )
        }
        composable(
            Screen.MaintenanceForm.route,
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
            Screen.DocumentList.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            DocumentListScreen(
                onDocumentClick = { documentId -> navHostController.navigate(Screen.DocumentDetail.createRoute(documentId)) },
                onAddClick = {
                    val assetId = it.arguments?.getString("assetId") ?: ""
                    navHostController.navigate(Screen.DocumentForm.createRoute(assetId = assetId))
                },
                onBack = { navHostController.popBackStack() }
            )
        }
        composable(
            Screen.DocumentDetail.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) {
            DocumentDetailScreen(
                onBack = { navHostController.popBackStack() },
                onDelete = { navHostController.popBackStack() }
            )
        }
        composable(
            Screen.DocumentForm.route,
            arguments = listOf(
                navArgument("assetId") { defaultValue = "" },
                navArgument("imageUri") { defaultValue = "" },
                navArgument("ocrText") { defaultValue = "" },
            ),
        ) {
            DocumentFormScreen(
                onSaved = { assetId ->
                    navHostController.navigate(Screen.DocumentList.createRoute(assetId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navHostController.popBackStack() }
            )
        }
    }
}