package com.modibo.keepguard.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(navHostController: NavHostController) {
    NavHost(navHostController, Screen.Home.route) {
        composable(Screen.Home.route) { Text("Home") }
        composable(Screen.AssetList.route) { Text("Asset List") }
        composable(Screen.Scanner.route) { Text("Scanner") }
        composable(Screen.Settings.route) { Text("Settings") }
        composable(
            "asset_detail/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { Text("Asset Detail") }
        composable(
            "asset_form?assetId={assetId}",
            arguments = listOf(navArgument("assetId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { Text("Asset Form") }
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