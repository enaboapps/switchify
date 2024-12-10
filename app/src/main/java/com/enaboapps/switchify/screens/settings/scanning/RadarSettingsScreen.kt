package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.PreferenceTimeStepper
import com.enaboapps.switchify.components.Section
import com.enaboapps.switchify.preferences.PreferenceManager

@Composable
fun RadarSettingsScreen(navController: NavController) {
    val preferenceManager = PreferenceManager(LocalContext.current)

    BaseView(
        title = "Radar Settings",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Section(title = "Radar Speed") {
                PreferenceTimeStepper(
                    value = preferenceManager.getLongValue(
                        PreferenceManager.PREFERENCE_KEY_RADAR_SCAN_RATE,
                        1000
                    ),
                    title = "Radar speed",
                    summary = "How fast the radar moves",
                    min = 10,
                    max = 5000,
                    step = 10,
                    onValueChanged = { newValue ->
                        preferenceManager.setLongValue(
                            PreferenceManager.PREFERENCE_KEY_RADAR_SCAN_RATE,
                            newValue
                        )
                    }
                )
            }
        }
    }
} 