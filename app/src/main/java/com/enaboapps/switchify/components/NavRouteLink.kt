package com.enaboapps.switchify.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun NavRouteLink(
    title: String,
    summary: String,
    navController: NavController,
    route: String
) {
    UICard(
        title = title.uppercase(),
        description = summary,
        rightIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = {
            navController.navigate(route)
        })
}