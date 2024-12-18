package com.enaboapps.switchify.service.scanning

import com.enaboapps.switchify.backend.preferences.PreferenceManager

interface ScanMethodObserver {
    fun onScanMethodChanged(type: String)
    fun onMenuStateChanged(isInMenu: Boolean)
}

/**
 * This object is used to manage the scanning method
 */
object ScanMethod {
    var preferenceManager: PreferenceManager? = null
    var observer: ScanMethodObserver? = null

    /**
     * This variable is used to determine if the scanning is in the menu
     */
    var isInMenu: Boolean = false
        set(value) {
            field = value
            observer?.onMenuStateChanged(value)
        }

    /**
     * This enum represents the type of the scanning method
     */
    object MethodType {
        /**
         * This type represents the cursor
         */
        const val CURSOR = "cursor"

        /**
         * This type represents the radar
         */
        const val RADAR = "radar"

        /**
         * This type represents the item scan
         * Sequentially scanning the items on the screen
         */
        const val ITEM_SCAN = "item_scan"
    }

    /**
     * This function is used to get the type of the scanning method
     */
    fun getType(): String {
        preferenceManager?.let { preferenceManager ->
            val storedType = preferenceManager.getStringValue(
                PreferenceManager.PREFERENCE_KEY_SCAN_METHOD
            )
            println("Stored type: $storedType")
            if (storedType.isNotEmpty()) {
                return storedType
            }
        }
        return MethodType.CURSOR
    }

    /**
     * This function gets the name of the scanning method
     * @param type The type of the scanning method
     * @return The name of the scanning method
     */
    fun getName(type: String): String {
        return when (type) {
            MethodType.CURSOR -> "Cursor"
            MethodType.RADAR -> "Radar"
            MethodType.ITEM_SCAN -> "Item Scan"
            else -> "Unknown"
        }
    }

    /**
     * This function gets the description of the scanning method
     * @param type The type of the scanning method
     * @return The description of the scanning method
     */
    fun getDescription(type: String): String {
        return when (type) {
            MethodType.CURSOR -> "Cursor allows you to select items by moving a set of crosshairs over the screen."
            MethodType.RADAR -> "Radar allows you to select items by moving a radar around the screen."
            MethodType.ITEM_SCAN -> "Item Scan allows you to select items by scanning through them sequentially."
            else -> "Unknown"
        }
    }

    /**
     * This function is used to set the type of the scanning method
     */
    fun setType(value: String) {
        preferenceManager?.setStringValue(
            PreferenceManager.PREFERENCE_KEY_SCAN_METHOD,
            value
        )
        observer?.onScanMethodChanged(value)
    }
}