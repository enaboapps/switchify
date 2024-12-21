package com.enaboapps.switchify.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.utils.Logger

@Composable
fun NavRouteLink(
    title: String,
    summary: String,
    navController: NavController,
    route: String
) {
    UICard(
        modifier = Modifier.padding(bottom = 8.dp),
        title = title.uppercase(),
        description = summary,
        rightIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = {
            navController.navigate(route)
            Logger.logEvent("Navigated to: $route")
        })
}