package com.enaboapps.switchify.service.scanning

import android.content.Context
import com.enaboapps.switchify.backend.preferences.PreferenceManager

/**
 * This class manages the scan colors
 */
class ScanColorManager {
    /**
     * This object represents a scan color set
     * @param name The name of the color set
     * @param primaryColor The primary color
     * @param secondaryColor The secondary color
     */
    data class ScanColorSet(val name: String, val primaryColor: String, val secondaryColor: String)

    companion object {
        /**
         * The scan color sets
         */
        val SCAN_COLOR_SETS = listOf(
            ScanColorSet("Blue and Red", "#0000FF", "#FF0000"),
            ScanColorSet("Green and Yellow", "#00FF00", "#FFFF00"),
            ScanColorSet("Purple and Orange", "#800080", "#FFA500"),
            ScanColorSet("Black and White", "#000000", "#FFFFFF"),
            ScanColorSet("Red and Blue", "#FF0000", "#0000FF"),
            ScanColorSet("Yellow and Green", "#FFFF00", "#00FF00"),
            ScanColorSet("Orange and Purple", "#FFA500", "#800080"),
            ScanColorSet("White and Black", "#FFFFFF", "#000000")
        )

        /**
         * Get the scan color set by name
         * @param name The name of the color set
         */
        fun getScanColorSetByName(name: String): ScanColorSet {
            SCAN_COLOR_SETS.forEach {
                if (it.name == name) {
                    return it
                }
            }
            return SCAN_COLOR_SETS[0]
        }

        /**
         * Get scan color set from preferences
         */
        fun getScanColorSetFromPreferences(context: Context): ScanColorSet {
            val preferenceManager = PreferenceManager(context)
            val scanColorSetName =
                preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_COLOR_SET)
            return getScanColorSetByName(scanColorSetName)
        }

        /**
         * Set scan color set to preferences
         * @param context The context of the caller
         * @param scanColorSetName The name of the color set
         */
        fun setScanColorSetToPreferences(context: Context, scanColorSetName: String) {
            val preferenceManager = PreferenceManager(context)
            preferenceManager.setStringValue(
                PreferenceManager.Keys.PREFERENCE_KEY_SCAN_COLOR_SET,
                scanColorSetName
            )
        }
    }
}