package com.enaboapps.switchify.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.components.FullWidthButton
import com.enaboapps.switchify.components.NavBar
import com.enaboapps.switchify.service.utils.ServiceUtils

@Composable
fun EnableAccessibilityServiceScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            NavBar(title = "Enable Accessibility Service", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "To use Switchify effectively, please enable the Accessibility Service in your device settings.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            val disclosure = context.resources.getString(R.string.accessibility_service_disclosure)
            Text(
                text = disclosure,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Take Me There", onClick = {
                ServiceUtils().openAccessibilitySettings(context)
            })
            FullWidthButton(text = "I've Enabled It", onClick = {
                navController.popBackStack()
            })
            FullWidthButton(text = "Not Right Now", onClick = {
                navController.popBackStack()
            })
        }
    }
}