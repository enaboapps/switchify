package com.enaboapps.switchify.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.models.SettingsScreenModel
import com.enaboapps.switchify.screens.settings.scanning.ScanMethodSelectionSection
import com.enaboapps.switchify.screens.settings.scanning.ScanModeSelectionSection
import com.enaboapps.switchify.widgets.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val settingsScreenModel = SettingsScreenModel(context)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            NavBar(title = "Settings", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                listOf("General", "Scanning", "Selection", "About").forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> GeneralSettingsTab(settingsScreenModel, navController)
                1 -> ScanningSettingsTab(settingsScreenModel, navController)
                2 -> SelectionSettingsTab(settingsScreenModel)
                3 -> AboutSection()
            }
        }
    }
}

@Composable
fun GeneralSettingsTab(settingsScreenModel: SettingsScreenModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        NavRouteLink(
            title = "Switches",
            summary = "Configure your switches",
            navController = navController,
            route = NavigationRoute.Switches.name
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavRouteLink(
            title = "Switch Stability",
            summary = "Configure switch stability settings",
            navController = navController,
            route = NavigationRoute.SwitchStability.name
        )
        Spacer(modifier = Modifier.height(16.dp))
        MenuSection(navController)
        ActionsSection(navController)
        KeyboardSection(navController)
        NotificationsSection(settingsScreenModel)
    }
}

@Composable
fun ScanningSettingsTab(settingsScreenModel: SettingsScreenModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScanMethodSelectionSection()
        ScanModeSelectionSection()
        TimingAndScanningSection(settingsScreenModel, navController)
        ItemScanSection(settingsScreenModel)
        ManualScanSection(settingsScreenModel)
        CursorSection(navController)
    }
}

@Composable
fun SelectionSettingsTab(settingsScreenModel: SettingsScreenModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SelectionSection(settingsScreenModel)
    }
}

@Composable
private fun TimingAndScanningSection(
    settingsScreenModel: SettingsScreenModel,
    navController: NavController
) {
    Section(title = "Timing and Scanning") {
        PreferenceTimeStepper(
            value = settingsScreenModel.scanRate.value ?: 0,
            title = "Scan rate",
            summary = "The interval at which the scanner will move to the next item",
            min = 200,
            max = 100000
        ) {
            settingsScreenModel.setScanRate(it)
        }
        PreferenceTimeStepper(
            value = settingsScreenModel.radarScanRate.value ?: 0,
            title = "Radar scan rate",
            summary = "The interval at which the radar will move",
            min = 10,
            max = 100000,
            step = 10
        ) {
            settingsScreenModel.setRadarScanRate(it)
        }
        PreferenceSwitch(
            title = "Automatically start scan after selection (auto scan only)",
            summary = "Automatically start the scan after a selection is made",
            checked = settingsScreenModel.automaticallyStartScanAfterSelection.value == true
        ) {
            settingsScreenModel.setAutomaticallyStartScanAfterSelection(it)
        }
        PreferenceSwitch(
            title = "Pause on first item",
            summary = "Pause scanning when the first item is highlighted",
            checked = settingsScreenModel.pauseOnFirstItem.value == true
        ) {
            settingsScreenModel.setPauseOnFirstItem(it)
        }
        if (settingsScreenModel.pauseOnFirstItem.value == true) {
            PreferenceTimeStepper(
                value = settingsScreenModel.pauseOnFirstItemDelay.value ?: 0,
                title = "Pause on first item delay",
                summary = "The delay to pause on the first item",
                min = 100,
                max = 100000
            ) {
                settingsScreenModel.setPauseOnFirstItemDelay(it)
            }
        }
        PreferenceSwitch(
            title = "Assisted selection",
            summary = "Assist the user in selecting items by selecting the closest available item to where they tap",
            checked = settingsScreenModel.assistedSelection.value == true,
            onCheckedChange = {
                settingsScreenModel.setAssistedSelection(it)
            }
        )
        NavRouteLink(
            title = "Scan Color",
            summary = "Configure the scan color",
            navController = navController,
            route = NavigationRoute.ScanColor.name
        )
    }
}

@Composable
private fun NotificationsSection(settingsScreenModel: SettingsScreenModel) {
    Section(title = "Notifications") {
        PreferenceSwitch(
            title = "Open notifications on switch press",
            summary = "When you receive a notification, you can open it by pressing the switch",
            checked = settingsScreenModel.openNotificationsOnSwitchPress.value == true,
            onCheckedChange = {
                settingsScreenModel.setOpenNotificationsOnSwitchPress(it)
            }
        )
    }
}

@Composable
private fun KeyboardSection(navController: NavController) {
    Section(title = "Keyboard") {
        NavRouteLink(
            title = "Choose Prediction Language",
            summary = "Choose the prediction language",
            navController = navController,
            route = NavigationRoute.PredictionLanguage.name
        )
    }
}

@Composable
private fun MenuSection(navController: NavController) {
    Section(title = "Menu") {
        NavRouteLink(
            title = "Customize Menu Items",
            summary = "Show or hide menu items",
            navController = navController,
            route = NavigationRoute.MenuItemCustomization.name
        )
    }
}

@Composable
private fun ActionsSection(navController: NavController) {
    Section(title = "Actions") {
        NavRouteLink(
            title = "My Actions",
            summary = "Customize your own actions",
            navController = navController,
            route = NavigationRoute.MyActions.name
        )
    }
}

@Composable
private fun CursorSection(navController: NavController) {
    Section(title = "Cursor") {
        NavRouteLink(
            title = "Cursor Settings",
            summary = "Configure the cursor settings",
            navController = navController,
            route = NavigationRoute.CursorSettings.name
        )
    }
}

@Composable
private fun SelectionSection(screenModel: SettingsScreenModel) {
    Section(title = "Selection") {
        PreferenceSwitch(
            title = "Auto select",
            summary = "Automatically select the item after a delay",
            checked = screenModel.autoSelect.value == true,
            onCheckedChange = {
                screenModel.setAutoSelect(it)
            }
        )
        PreferenceTimeStepper(
            value = screenModel.autoSelectDelay.value ?: 0,
            title = "Auto select delay",
            summary = "The delay before the item is selected",
            min = 100,
            max = 100000
        ) {
            screenModel.setAutoSelectDelay(it)
        }
    }
}

@Composable
private fun ItemScanSection(screenModel: SettingsScreenModel) {
    Section(title = "Item Scan") {
        PreferenceSwitch(
            title = "Row column scan",
            summary = "Scan items in a row column pattern",
            checked = screenModel.rowColumnScan.value == true,
            onCheckedChange = {
                screenModel.setRowColumnScan(it)
            }
        )
        PreferenceSwitch(
            title = "Group scan (requires row column scan)",
            summary = "Scan items in a group pattern",
            checked = screenModel.groupScan.value == true,
            onCheckedChange = {
                screenModel.setGroupScan(it)
            }
        )
    }
}

@Composable
private fun ManualScanSection(screenModel: SettingsScreenModel) {
    Section(title = "Manual Scan") {
        PreferenceSwitch(
            title = "Move Repeat",
            summary = "Hold down a switch to move repeatedly",
            checked = screenModel.moveRepeat.value == true,
            onCheckedChange = {
                screenModel.setMoveRepeat(it)
            }
        )
        PreferenceTimeStepper(
            value = screenModel.moveRepeatDelay.value ?: 0,
            title = "Move Repeat Delay",
            summary = "The delay before repeating the last move action",
            min = 100,
            max = 100000,
            step = 100
        ) {
            screenModel.setMoveRepeatDelay(it)
        }
    }
}

@Composable
fun AboutSection() {
    val context = LocalContext.current
    val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    val websiteUrl = "https://switchifyapp.com"
    val repositoryUrl = "https://github.com/enaboapps/switchify"
    val privacyPolicyUrl = "https://www.enaboapps.com/switchify-privacy-policy"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Version $version",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        FullWidthButton(text = "Website", onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
        })
        Spacer(modifier = Modifier.height(16.dp))
        FullWidthButton(
            text = "View on GitHub",
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repositoryUrl)))
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        FullWidthButton(
            text = "Privacy Policy",
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)))
            }
        )
    }
}