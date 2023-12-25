package com.enaboapps.switchify.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enaboapps.switchify.screens.EnableAccessibilityServiceScreen
import com.enaboapps.switchify.screens.HomeScreen
import com.enaboapps.switchify.screens.settings.AddNewSwitchScreen
import com.enaboapps.switchify.screens.settings.SettingsScreen
import com.enaboapps.switchify.screens.settings.SwitchesScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = NavigationRoute.Home.name) {
        composable(NavigationRoute.Home.name) {
            HomeScreen(navController)
        }
        composable(NavigationRoute.Settings.name) {
            SettingsScreen(navController)
        }
        composable(NavigationRoute.Switches.name) {
            SwitchesScreen(navController)
        }
        composable(NavigationRoute.AddNewSwitch.name) {
            AddNewSwitchScreen(navController)
        }
        composable(NavigationRoute.EnableAccessibilityService.name) {
            EnableAccessibilityServiceScreen()
        }
    }
}