package com.enaboapps.switchify.nav

sealed class NavigationRoute(val name: String) {

    data object Home : NavigationRoute("Home")
    data object Setup : NavigationRoute("Setup")
    data object Settings : NavigationRoute("Settings")
    data object Switches : NavigationRoute("Switches")
    data object AddNewSwitch : NavigationRoute("AddNewSwitch")
    data object EditSwitch : NavigationRoute("EditSwitch")
    data object EnableAccessibilityService : NavigationRoute("EnableAccessibilityService")

}
