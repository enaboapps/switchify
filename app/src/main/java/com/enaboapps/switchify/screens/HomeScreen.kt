package com.enaboapps.switchify.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.backend.iap.IAPHandler
import com.enaboapps.switchify.backend.preferences.PreferenceManager
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.NavBarAction
import com.enaboapps.switchify.components.NavRouteLink
import com.enaboapps.switchify.keyboard.utils.KeyboardUtils
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.service.utils.ServiceUtils
import com.enaboapps.switchify.switches.SwitchConfigInvalidBanner
import com.enaboapps.switchify.switches.SwitchEventStore
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
fun HomeScreen(navController: NavController, serviceUtils: ServiceUtils = ServiceUtils()) {
    val context = LocalContext.current
    val isAccessibilityServiceEnabled = serviceUtils.isAccessibilityServiceEnabled(context)
    val isSwitchifyKeyboardEnabled = KeyboardUtils.isSwitchifyKeyboardEnabled(context)
    val isSetupComplete = PreferenceManager(context).isSetupComplete()
    val isPro = remember { mutableStateOf(false) }
    val signedIn = AuthManager.instance.isUserSignedIn()

    LaunchedEffect(Unit) {
        if (!isSetupComplete && !signedIn) {
            navController.navigate(NavigationRoute.Setup.name)
        } else if (signedIn) {
            PreferenceManager(context).setSetupComplete()
        }
        isPro.value = IAPHandler.hasPurchasedPro()
    }

    var showUpdateDialog by remember { mutableStateOf(false) }
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    val installStateUpdatedListener = remember {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    Log.d("HomeScreen", "Update downloaded")
                    showUpdateDialog = true
                }

                InstallStatus.FAILED -> {
                    Log.e("HomeScreen", "Update failed! State: ${state.installErrorCode()}")
                }

                InstallStatus.INSTALLED -> {
                    Log.d("HomeScreen", "Update installed successfully")
                }

                else -> {
                    Log.d("HomeScreen", "Install Status: ${state.installStatus()}")
                }
            }
        }
    }

    val updateResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d("HomeScreen", "Update flow started successfully")
                Toast.makeText(
                    context,
                    "Downloading update...",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Activity.RESULT_CANCELED -> {
                Log.d("HomeScreen", "Update cancelled by user")
                Toast.makeText(
                    context,
                    "Update cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Log.e("HomeScreen", "Update flow failed! Result code: ${result.resultCode}")
                Toast.makeText(
                    context,
                    "Update failed to start",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val reviewManager = remember { ReviewManagerFactory.create(context) }

    LaunchedEffect(Unit) {
        appUpdateManager.registerListener(installStateUpdatedListener)
        checkForUpdates(context, appUpdateManager, updateResultLauncher)
        checkForDownloadedUpdate(appUpdateManager) { showUpdateDialog = it }
        requestReview(context, reviewManager)
    }

    DisposableEffect(Unit) {
        onDispose {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }

    BaseView(
        title = "Switchify",
        navController = navController,
        navBarActions = listOf(NavBarAction(text = "Feedback", onClick = {
            val url = "https://switchify.featurebase.app/"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }))
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        NavRouteLink(
            title = "Settings",
            summary = "Tap here to adjust your settings.",
            navController = navController,
            route = NavigationRoute.Settings.name
        )

        SwitchConfigInvalidBanner(SwitchEventStore(LocalContext.current).isConfigInvalid())


        if (!isAccessibilityServiceEnabled) {
            NavRouteLink(
                title = "Accessibility Service",
                summary = "Tap here to enable the accessibility service.",
                navController = navController,
                route = NavigationRoute.EnableAccessibilityService.name
            )
        }

        if (!isSwitchifyKeyboardEnabled) {
            NavRouteLink(
                title = "Switchify Keyboard",
                summary = "Tap here to enable the Switchify keyboard.",
                navController = navController,
                route = NavigationRoute.EnableSwitchifyKeyboard.name
            )
        }

        if (!isPro.value) {
            NavRouteLink(
                title = "Upgrade to Pro",
                summary = "Upgrade to Pro to unlock new features and support Switchify.",
                navController = navController,
                route = NavigationRoute.Paywall.name
            )
        }

        AccountCard(navController)

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

private fun checkForUpdates(
    context: Context,
    appUpdateManager: AppUpdateManager,
    updateResultLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
) {
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
                    Log.d("HomeScreen", "Update available. Requesting update.")
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error starting update flow", e)
                    Toast.makeText(
                        context,
                        "Failed to start update process",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d("HomeScreen", "No update available or update type not allowed")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("HomeScreen", "Failed to check for updates", exception)
            Toast.makeText(
                context,
                "Failed to check for updates",
                Toast.LENGTH_SHORT
            ).show()
        }
}

private fun checkForDownloadedUpdate(
    appUpdateManager: AppUpdateManager,
    callback: (Boolean) -> Unit
) {
    appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            callback(true)
        }
    }
}

/**
 * Account card
 * If the user is logged in, show the account email and on click, go to the account screen
 * If the user is not logged in, on click, launch the sign in intent
 */
@Composable
fun AccountCard(navController: NavController) {
    val authManager = AuthManager.instance

    val isUserSignedIn = authManager.isUserSignedIn()
    val currentUser = authManager.getCurrentUser()

    val title = if (isUserSignedIn) "Account" else "Sign In"
    val description =
        if (isUserSignedIn) currentUser?.email ?: "" else "Sign in to access your settings."

    NavRouteLink(
        title = title,
        summary = description,
        navController = navController,
        route = if (isUserSignedIn) NavigationRoute.Account.name else NavigationRoute.SignIn.name
    )
}

private fun requestReview(context: Context, reviewManager: ReviewManager) {
    val request = reviewManager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            reviewManager.launchReviewFlow(context as Activity, reviewInfo)
                .addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    Log.d("HomeScreen", "Review flow completed")
                }
        } else {
            Log.e("HomeScreen", "Error requesting review flow", task.exception)
        }
    }
}
