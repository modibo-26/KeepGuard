package com.modibo.keepguard.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.modibo.keepguard.presentation.screen.assets.detail.AssetDetailScreen
import com.modibo.keepguard.presentation.screen.assets.form.AssetFormScreen
import com.modibo.keepguard.presentation.screen.assets.list.AssetListScreen

@Composable
fun NavGraph(navHostController: NavHostController) {
    NavHost(navHostController, Screen.Home.route) {
        composable(Screen.Home.route) { Text("Home") }
        composable(Screen.AssetList.route) {
            AssetListScreen(
                onAssetClick = { assetId -> navHostController.navigate("asset_detail/$assetId") },
                onAddClick = { navHostController.navigate("asset_form") }
            )
        }
        composable(Screen.Scanner.route) { Text("Scanner - bientôt disponible") }
        composable(Screen.Settings.route) { Text("Settings") }
        composable(
            "asset_detail/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            AssetDetailScreen(
                onBack = { navHostController.popBackStack() },
                onEdit = { assetId -> navHostController.navigate("asset_form?assetId=$assetId") },
                onDelete = { navHostController.popBackStack() },
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
            "warranty_form/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { Text("Warranty Form") }
        composable(
            "maintenance_form/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { Text("Maintenance Form") }
    }
}