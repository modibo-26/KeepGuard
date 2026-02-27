package com.modibo.keepguard.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavbar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar() {
        NavigationBarItem(
            currentRoute == Screen.Home.route,
            { navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
            },
            { Icon(Icons.Default.Home, "Accueil") },
            label = { Text("Accueil") },
        )
        NavigationBarItem(
            currentRoute == Screen.AssetList.route,
            { navController.navigate(Screen.AssetList.route)  {
                popUpTo(Screen.AssetList.route) { inclusive = true }
                launchSingleTop = true
            }},
            { Icon(Icons.Default.Inventory2, "Mes biens") },
            label = { Text("Mes biens") },
        )
        NavigationBarItem(
            currentRoute == Screen.Scanner.route,
            { navController.navigate(Screen.Scanner.route)  {
                popUpTo(Screen.Scanner.route) { inclusive = true }
                launchSingleTop = true
            }},
            { Icon(Icons.Default.CameraAlt, "Scanner") },
            label = { Text("Scanner") },
        )
        NavigationBarItem(
            currentRoute == Screen.Settings.route,
            { navController.navigate(Screen.Settings.route)  {
                popUpTo(Screen.Settings.route) { inclusive = true }
                launchSingleTop = true
            }},
            { Icon(Icons.Default.Settings, "Paramètres") },
            label = { Text("Paramètres") },
        )
    }
}