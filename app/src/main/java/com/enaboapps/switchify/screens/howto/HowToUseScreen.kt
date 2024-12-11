package com.enaboapps.switchify.screens.howto

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.InfoCard
import com.enaboapps.switchify.components.NavRouteLink
import com.enaboapps.switchify.components.Section
import com.enaboapps.switchify.nav.NavigationRoute

@Composable
fun HowToUseScreen(navController: NavController) {
    BaseView(
        title = "How To Use",
        navController = navController
    ) {
        Section(title = "Step 1") {
            NavRouteLink(
                title = "Add Switches",
                summary = "Add your switches to the app",
                navController = navController,
                route = NavigationRoute.Switches.name
            )
            InfoCard(
                title = "What are switches?",
                description = "Switches are physical buttons that are connected to your device. You can add them to the app and use them to control your device."
            )
        }
        Section(title = "Step 2") {
            NavRouteLink(
                title = "Enable Accessibility Service",
                summary = "Enable the Switchify Accessibility Service",
                navController = navController,
                route = NavigationRoute.EnableAccessibilityService.name
            )
            InfoCard(
                title = "What is the Accessibility Service?",
                description = "The Accessibility Service is a system service that allows Switchify to detect when you press a switch."
            )
        }
        Section(title = "Step 3") {
            NavRouteLink(
                title = "Enable Keyboard",
                summary = "Enable the Switchify Keyboard",
                navController = navController,
                route = NavigationRoute.EnableSwitchifyKeyboard.name
            )
            InfoCard(
                title = "What is the Switchify Keyboard?",
                description = "The Switchify Keyboard is a virtual keyboard that allows you to control your device using your switches."
            )
        }
    }
}