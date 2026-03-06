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
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavbar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar() {
        NavigationBarItem(
            currentRoute == Screen.Home.route,
            {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
            },
            { Icon(Icons.Default.Home, "Accueil") },
            label = { Text("Accueil") },
        )
        NavigationBarItem(
            currentRoute == Screen.AssetList.route,
            {
                if (currentRoute != Screen.AssetList.route) {
                    navController.navigate(Screen.AssetList.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
            },
            { Icon(Icons.Default.Inventory2, "Mes biens") },
            label = { Text("Mes biens") },
        )
        NavigationBarItem(
            currentRoute == Screen.Scanner.route,
            {
                if (currentRoute != Screen.Scanner.route) {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
            },
            { Icon(Icons.Default.CameraAlt, "Scanner") },
            label = { Text("Scanner") },
        )
        NavigationBarItem(
            currentRoute == Screen.Settings.route,
            {
                if (currentRoute != Screen.Settings.route) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
            },
            { Icon(Icons.Default.Settings, "Paramètres") },
            label = { Text("Paramètres") },
        )
    }
}