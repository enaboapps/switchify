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
import com.enaboapps.switchify.components.PreferenceSwitch
import com.enaboapps.switchify.components.PreferenceTimeStepper
import com.enaboapps.switchify.components.Section
import com.enaboapps.switchify.preferences.PreferenceManager

@Composable
fun ItemScanSettingsScreen(navController: NavController) {
    val preferenceManager = PreferenceManager(LocalContext.current)

    BaseView(
        title = "Item Scan Settings",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Section(title = "Scan Pattern") {
                PreferenceSwitch(
                    title = "Row-column scanning",
                    summary = "First scan rows, then scan items in the selected row",
                    checked = preferenceManager.getBooleanValue(
                        PreferenceManager.PREFERENCE_KEY_ROW_COLUMN_SCAN,
                        false
                    ),
                    onCheckedChange = {
                        preferenceManager.setBooleanValue(
                            PreferenceManager.PREFERENCE_KEY_ROW_COLUMN_SCAN,
                            it
                        )
                    }
                )

                PreferenceSwitch(
                    title = "Group scanning",
                    summary = "Scan items in groups for faster selection",
                    checked = preferenceManager.getBooleanValue(
                        PreferenceManager.PREFERENCE_KEY_GROUP_SCAN,
                        false
                    ),
                    onCheckedChange = {
                        preferenceManager.setBooleanValue(
                            PreferenceManager.PREFERENCE_KEY_GROUP_SCAN,
                            it
                        )
                    }
                )
            }

            Section(title = "Timing") {
                PreferenceTimeStepper(
                    value = preferenceManager.getLongValue(
                        PreferenceManager.PREFERENCE_KEY_SCAN_RATE,
                        1000
                    ),
                    title = "Scan rate",
                    summary = "How long to highlight each item before moving to the next",
                    min = 200,
                    max = 10000,
                    step = 100,
                    onValueChanged = { newValue ->
                        preferenceManager.setLongValue(
                            PreferenceManager.PREFERENCE_KEY_SCAN_RATE,
                            newValue
                        )
                    }
                )
            }
        }
    }
} 