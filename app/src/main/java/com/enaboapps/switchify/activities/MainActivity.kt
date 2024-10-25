package com.enaboapps.switchify.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.enaboapps.switchify.activities.ui.theme.SwitchifyTheme
import com.enaboapps.switchify.nav.NavGraph
import com.enaboapps.switchify.preferences.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var preferenceManager: PreferenceManager
    private var showUpdateDialog by mutableStateOf(false)

    companion object {
        private const val TAG = "MainActivity"
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update downloaded")
                showUpdateDialog = true
            }

            InstallStatus.FAILED -> {
                Log.e(TAG, "Update failed! State: ${state.installErrorCode()}")
            }

            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed successfully")
            }

            else -> {
                Log.d(TAG, "Install Status: ${state.installStatus()}")
            }
        }
    }

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "Update flow started successfully")
                Toast.makeText(
                    this,
                    "Downloading update...",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Update cancelled by user")
                Toast.makeText(
                    this,
                    "Update cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Log.e(TAG, "Update flow failed! Result code: ${result.resultCode}")
                Toast.makeText(
                    this,
                    "Update failed to start",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeManagers()

        setContent {
            val navController = rememberNavController()

            SwitchifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavGraph(
                                navController = navController
                            )

                            if (showUpdateDialog) {
                                AlertDialog(
                                    onDismissRequest = { showUpdateDialog = false },
                                    title = { Text("Update Available") },
                                    text = { Text("A new version has been downloaded. Restart now to complete the installation?") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showUpdateDialog = false
                                                appUpdateManager.completeUpdate()
                                            }
                                        ) {
                                            Text("Restart")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showUpdateDialog = false }
                                        ) {
                                            Text("Later")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Check for downloaded updates
                LaunchedEffect(Unit) {
                    checkForDownloadedUpdate()
                }

                // Cleanup listener when the composable is disposed
                DisposableEffect(Unit) {
                    onDispose {
                        appUpdateManager.unregisterListener(installStateUpdatedListener)
                    }
                }
            }
        }

        // Check for updates
        checkForUpdates()
    }

    private fun initializeManagers() {
        // Initialize PreferenceManager
        preferenceManager = PreferenceManager(this)
        preferenceManager.preferenceSync.apply {
            retrieveSettingsFromFirestore()
            listenForSettingsChangesOnRemote()
        }

        // Initialize AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    private fun checkForUpdates() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    try {
                        val updateOptions =
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateResultLauncher,
                            updateOptions
                        )
                        Log.d(TAG, "Update available. Requesting update.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting update flow", e)
                        Toast.makeText(
                            this,
                            "Failed to start update process",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d(TAG, "No update available or update type not allowed")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to check for updates", exception)
                Toast.makeText(
                    this,
                    "Failed to check for updates",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkForDownloadedUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateDialog = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkForDownloadedUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}