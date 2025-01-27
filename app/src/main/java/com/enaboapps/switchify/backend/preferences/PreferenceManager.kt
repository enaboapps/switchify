package com.enaboapps.switchify.backend.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    companion object Keys {
        const val PREFERENCE_KEY_SETUP_COMPLETE = "setup_complete"
        const val PREFERENCE_KEY_PRO = "pro"
        const val PREFERENCE_KEY_SCAN_MODE = "scan_mode"
        const val PREFERENCE_KEY_SCAN_RATE = "scan_rate"
        const val PREFERENCE_KEY_SCAN_CYCLES = "scan_cycles"
        const val PREFERENCE_KEY_SCAN_METHOD = "scan_method"
        const val PREFERENCE_KEY_CURSOR_FINE_SCAN_RATE = "cursor_fine_scan_rate"
        const val PREFERENCE_KEY_CURSOR_BLOCK_SCAN_RATE = "cursor_block_scan_rate"
        const val PREFERENCE_KEY_RADAR_SCAN_RATE = "radar_scan_rate"
        const val PREFERENCE_KEY_SWITCH_HOLD_TIME = "switch_hold_time"
        const val PREFERENCE_KEY_MOVE_REPEAT = "move_repeat"
        const val PREFERENCE_KEY_MOVE_REPEAT_DELAY = "move_repeat_delay"
        const val PREFERENCE_KEY_AUTOMATICALLY_START_SCAN_AFTER_SELECTION =
            "automatically_start_scan_after_selection"
        const val PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM = "pause_on_first_item"
        const val PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY = "pause_on_first_item_delay"
        const val PREFERENCE_KEY_GROUP_SCAN = "group_scan"
        const val PREFERENCE_KEY_AUTO_SELECT = "auto_select"
        const val PREFERENCE_KEY_AUTO_SELECT_DELAY = "auto_select_delay"
        const val PREFERENCE_KEY_ASSISTED_SELECTION = "assisted_selection"
        const val PREFERENCE_KEY_CURSOR_MODE = "cursor_mode"
        const val PREFERENCE_KEY_ROW_COLUMN_SCAN = "row_column_scan"
        const val PREFERENCE_KEY_ITEM_SCAN_SPEECH = "item_scan_speech"
        const val PREFERENCE_KEY_SWITCH_IGNORE_REPEAT = "switch_ignore_repeat"
        const val PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY = "switch_ignore_repeat_delay"
        const val PREFERENCE_KEY_SCAN_COLOR_SET = "scan_color_set"
        const val PREFERENCE_KEY_MENU_ITEM_VISIBILITY_PREFIX = "menu_item_visibility_"
        const val PREFERENCE_KEY_MENU_SIZE = "menu_size"
        const val PREFERENCE_KEY_MENU_TRANSPARENCY = "menu_transparency"
        const val PREFERENCE_KEY_LOCK_SCREEN = "lock_screen"
        const val PREFERENCE_KEY_LOCK_SCREEN_CODE = "lock_screen_code"
        private const val PREFERENCE_FILE_NAME = "switchify_preferences"
    }

    private val appContext = context.applicationContext

    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)

    val preferenceSync = PreferenceSync(sharedPreferences)

    fun setSetupComplete() {
        setBooleanValue(PREFERENCE_KEY_SETUP_COMPLETE, true)
    }

    fun isSetupComplete(): Boolean {
        return getBooleanValue(PREFERENCE_KEY_SETUP_COMPLETE)
    }

    fun setIntegerValue(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            apply()
            preferenceSync.uploadSettingsToFirestore()
        }
    }

    fun setFloatValue(key: String, value: Float) {
        with(sharedPreferences.edit()) {
            putFloat(key, value)
            apply()
            preferenceSync.uploadSettingsToFirestore()
        }
    }

    fun setBooleanValue(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
            preferenceSync.uploadSettingsToFirestore()
        }
    }

    fun setLongValue(key: String, value: Long) {
        with(sharedPreferences.edit()) {
            putLong(key, value)
            apply()
            preferenceSync.uploadSettingsToFirestore()
        }
    }

    fun setStringValue(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
            preferenceSync.uploadSettingsToFirestore()
        }
    }

    fun getFloatValue(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getIntegerValue(key: String, defaultValue: Int = 1000): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getLongValue(key: String, defaultValue: Long = 1000L): Long {
        // Due to an old version of the app storing some values as different types, we need to do try/catch
        return try {
            sharedPreferences.getLong(key, defaultValue)
        } catch (e: ClassCastException) {
            defaultValue
        }
    }

    fun getStringValue(key: String, defaultValue: String = ""): String {
        // Due to an old version of the app storing some values as different types, we need to do try/catch
        return try {
            sharedPreferences.getString(key, defaultValue) ?: defaultValue
        } catch (e: ClassCastException) {
            defaultValue
        }
    }

    fun setMenuItemVisibility(menuItemId: String, isVisible: Boolean) {
        setBooleanValue(PREFERENCE_KEY_MENU_ITEM_VISIBILITY_PREFIX + menuItemId, isVisible)
    }

    fun getMenuItemVisibility(menuItemId: String, defaultValue: Boolean = true): Boolean {
        return getBooleanValue(
            PREFERENCE_KEY_MENU_ITEM_VISIBILITY_PREFIX + menuItemId,
            defaultValue
        )
    }
}
