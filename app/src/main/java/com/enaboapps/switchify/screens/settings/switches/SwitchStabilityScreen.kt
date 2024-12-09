package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.components.NavBar
import com.enaboapps.switchify.components.PreferenceSwitch
import com.enaboapps.switchify.components.PreferenceTimeStepper
import com.enaboapps.switchify.screens.settings.switches.models.SwitchStabilityScreenModel

@Composable
fun SwitchStabilityScreen(navController: NavController) {
    val verticalScrollState = rememberScrollState()
    val context = LocalContext.current
    val switchStabilityScreenModel = SwitchStabilityScreenModel(context)
    val pausedScanOnSwitchHold = switchStabilityScreenModel.pauseScanOnSwitchHold.observeAsState()
    val ignoredRepeat = switchStabilityScreenModel.switchIgnoreRepeat.observeAsState()
    Scaffold(
        topBar = {
            NavBar(title = "Switch Stability", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(paddingValues)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            if (switchStabilityScreenModel.shouldShowPauseScanOnSwitchHold()) {
                PreferenceSwitch(
                    title = "Pause scan on switch hold",
                    summary = "Pause scanning when a switch is held",
                    checked = pausedScanOnSwitchHold.value == true
                ) {
                    switchStabilityScreenModel.setPauseScanOnSwitchHold(it)
                }
            }
            PreferenceSwitch(
                title = "Ignore repeat",
                summary = "Ignore repeated switch presses",
                checked = ignoredRepeat.value == true
            ) {
                switchStabilityScreenModel.setSwitchIgnoreRepeat(it)
            }
            if (ignoredRepeat.value == true) {
                PreferenceTimeStepper(
                    title = "Ignore repeat delay",
                    summary = "The time to ignore repeated switch presses",
                    min = 100,
                    max = 10000,
                    value = switchStabilityScreenModel.switchIgnoreRepeatDelay.observeAsState().value
                        ?: 0
                ) {
                    switchStabilityScreenModel.setSwitchIgnoreRepeatDelay(it)
                }
            }
            PreferenceTimeStepper(
                title = "Switch hold time",
                summary = "The time to hold a switch before a long press is registered (this is the same between each long press action)",
                min = 100,
                max = 10000,
                value = switchStabilityScreenModel.switchHoldTime.observeAsState().value ?: 0
            ) {
                switchStabilityScreenModel.setSwitchHoldTime(it)
            }
        }
    }
}