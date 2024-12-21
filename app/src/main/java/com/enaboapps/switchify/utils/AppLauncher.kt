package com.enaboapps.switchify.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * AppLauncher class provides functionality to launch Android apps by their display name.
 *
 * @property context The Android application context.
 */
class AppLauncher(private val context: Context) {

    /**
     * Retrieves a list of all installed apps on the device.
     *
     * @return List of AppInfo objects containing display names and package names of installed apps.
     */
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        // Query the package manager for all apps with a launcher intent
        return packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            AppInfo(
                resolveInfo.loadLabel(packageManager).toString(),
                resolveInfo.activityInfo.packageName
            )
        }
    }

    /**
     * Attempts to launch an app by its display name.
     * If the app is not found, it logs an error and optionally opens the Play Store.
     *
     * @param displayName The display name of the app to launch.
     */
    fun launchAppByDisplayName(displayName: String) {
        val installedApps = getInstalledApps()
        val app = installedApps.find { it.displayName.equals(displayName, ignoreCase = true) }

        if (app != null) {
            launchAppByPackageName(app.packageName)
        } else {
            Log.e("AppLauncher", "App not found: $displayName")
            // Optionally open the Play Store to search for the app
            openPlayStoreSearch(displayName)
        }
    }

    /**
     * Finds the package name of an app by its display name.
     * Name can be a partial match, so it's case-insensitive.
     * If the app is not found, it launches the Play Store search.
     *
     * @param displayName The display name of the app to find.
     * @return The package name of the app, or null if not found.
     */
    fun findPackageNameByDisplayName(displayName: String): String? {
        val installedApps = getInstalledApps()
        val app = installedApps.find { it.displayName.equals(displayName, ignoreCase = true) }

        if (app != null) {
            return app.packageName
        } else {
            Log.e("AppLauncher", "App not found: $displayName")
            // Optionally open the Play Store to search for the app
            openPlayStoreSearch(displayName)
            return null
        }
    }

    /**
     * Launches an app given its package name.
     *
     * @param packageName The package name of the app to launch.
     */
    fun launchAppByPackageName(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Logger.logEvent("Launched app: $packageName")
            } else {
                Log.e("AppLauncher", "Unable to launch app: $packageName")
            }
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error launching app: ${e.message}")
        }
    }

    /**
     * Opens the Google Play Store with a search query for the given app name.
     *
     * @param searchQuery The app name to search for in the Play Store.
     */
    private fun openPlayStoreSearch(searchQuery: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?q=$searchQuery")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Logger.logEvent("Opened Play Store search for: $searchQuery")
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error opening Play Store: ${e.message}")
        }
    }

    /**
     * Data class to hold information about an installed app.
     *
     * @property displayName The user-visible name of the app.
     * @property packageName The unique identifier of the app on the device.
     */
    data class AppInfo(val displayName: String, val packageName: String)
}