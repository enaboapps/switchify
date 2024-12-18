package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.Picker
import com.enaboapps.switchify.components.PreferenceSwitch
import com.enaboapps.switchify.components.PreferenceTimeStepper
import com.enaboapps.switchify.components.Section
import com.enaboapps.switchify.backend.preferences.PreferenceManager

@Composable
fun ItemScanSettingsScreen(navController: NavController) {
    val preferenceManager = PreferenceManager(LocalContext.current)
    var currentScanCycles = remember {
        mutableStateOf(
            preferenceManager.getStringValue(
                PreferenceManager.Keys.PREFERENCE_KEY_SCAN_CYCLES,
                "3"
            )
        )
    }

    BaseView(
        title = "Item Scan Settings",
        navController = navController
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

            PreferenceSwitch(
                title = "Speak items while scanning",
                summary = "Speak the item content description while scanning",
                checked = preferenceManager.getBooleanValue(
                    PreferenceManager.PREFERENCE_KEY_ITEM_SCAN_SPEECH,
                    false
                ),
                onCheckedChange = {
                    preferenceManager.setBooleanValue(
                        PreferenceManager.PREFERENCE_KEY_ITEM_SCAN_SPEECH,
                        it
                    )
                }
            )

            Picker(
                title = "Scan cycles",
                selectedItem = currentScanCycles.value,
                items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
                onItemSelected = { item ->
                    currentScanCycles.value = item
                    preferenceManager.setStringValue(
                        PreferenceManager.Keys.PREFERENCE_KEY_SCAN_CYCLES,
                        item
                    )
                },
                itemToString = { it.toString() },
                itemDescription = { "Scan $it times before stopping" }
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