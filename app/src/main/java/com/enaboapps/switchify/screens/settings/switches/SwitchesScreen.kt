package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.components.LoadingIndicator
import com.enaboapps.switchify.components.NavBar
import com.enaboapps.switchify.components.NavBarAction
import com.enaboapps.switchify.components.NavRouteLink
import com.enaboapps.switchify.components.Section
import com.enaboapps.switchify.components.UICard
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.switches.models.SwitchesScreenModel
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

@Composable
fun SwitchesScreen(navController: NavController) {
    val switchesScreenModel = SwitchesScreenModel(
        SwitchEventStore(LocalContext.current)
    )

    val uiState by switchesScreenModel.uiState.collectAsState()
    val verticalScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            NavBar(
                title = "Switches",
                navController = navController,
                actions = listOf(
                    NavBarAction(
                        text = "Test Switches",
                        onClick = {
                            navController.navigate(NavigationRoute.TestSwitches.name)
                        }
                    )
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(NavigationRoute.AddNewSwitch.name)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .padding(paddingValues)
                        .padding(all = 16.dp),
                ) {
                    if (uiState.localSwitches.isEmpty()) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No switches found",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        Section(title = "Switches") {
                            uiState.localSwitches.forEach { event ->
                                SwitchEventItem(
                                    navController = navController,
                                    switchEvent = event
                                )
                            }
                        }
                    }

                    // Display remote switches that aren't on device
                    val availableRemoteSwitches = uiState.remoteSwitches.filter { !it.isOnDevice }
                    if (availableRemoteSwitches.isNotEmpty()) {
                        Section(title = "Previously Used Switches") {
                            availableRemoteSwitches.forEach { remoteSwitch ->
                                RemoteSwitchItem(
                                    model = switchesScreenModel,
                                    remoteSwitch = remoteSwitch,
                                    isImporting = uiState.importingSwitch == remoteSwitch.code
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteSwitchItem(
    model: SwitchesScreenModel,
    remoteSwitch: SwitchEventStore.RemoteSwitchInfo,
    isImporting: Boolean
) {
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UICard(
            modifier = Modifier.weight(1f),
            title = remoteSwitch.name,
            description = if (isImporting) "Importing..." else "Tap to add",
            onClick = {
                if (!isImporting) {
                    model.importSwitch(remoteSwitch)
                }
            },
            enabled = !isImporting
        )
        if (!isImporting) {
            IconButton(onClick = {
                showDeleteConfirmation.value = true
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Switch",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        if (showDeleteConfirmation.value) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation.value = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this switch from your account?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation.value = false
                            model.deleteRemoteSwitch(remoteSwitch)
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation.value = false
                        }
                    ) {
                        Text("Cancel")
                    }
                })
        }
    }
}

@Composable
private fun SwitchEventItem(
    navController: NavController,
    switchEvent: SwitchEvent
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        NavRouteLink(
            title = switchEvent.name,
            summary = "Edit this switch",
            navController = navController,
            route = "${NavigationRoute.EditSwitch.name}/${switchEvent.code}"
        )
    }
}