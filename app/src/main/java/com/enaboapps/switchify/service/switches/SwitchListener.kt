package com.enaboapps.switchify.service.switches

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.notifications.NotificationManager
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.selection.SelectionHandler
import com.enaboapps.switchify.service.window.ServiceMessageHUD
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

/**
 * Manages switch input events and their associated actions in the accessibility service.
 * Handles switch presses, releases, long presses, and notification interactions.
 *
 * @property context Application context used for accessing system services and resources
 * @property scanningManager Manages the scanning interface for switch-based navigation
 * @property notificationManager Handles system notifications and their presentation
 * @property scanSettings Contains configuration for scanning behavior and switch interactions
 */
class SwitchListener(
    private val context: Context,
    private val scanningManager: ScanningManager,
    private val notificationManager: NotificationManager,
    private val scanSettings: ScanSettings
) {
    private val preferenceManager = PreferenceManager(context)
    private val switchEventStore = SwitchEventStore(context)

    /** Tracks the most recent switch action for handling release events */
    private var latestAction: AbsorbedSwitchAction? = null

    /** Timestamp of the last switch press for handling repeat events */
    private var lastSwitchPressedTime: Long = 0

    /** Key code of the last pressed switch for handling repeat events */
    private var lastSwitchPressedCode: Int = 0

    /**
     * BroadcastReceiver that listens for updates to switch event configurations
     * and reloads the switch event store when changes occur.
     */
    private val switchEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            switchEventStore.reload()
        }
    }

    init {
        // Register for switch event configuration updates
        LocalBroadcastManager.getInstance(context).registerReceiver(
            switchEventReceiver,
            IntentFilter(SwitchEventStore.EVENTS_UPDATED)
        )

        // Set up notification listener for switch-based notification handling
        notificationManager.setListener(object : NotificationManager.NotificationListener {
            override fun onNotificationReceived(notification: NotificationManager.NotificationInfo) {
                if (scanSettings.isOpenNotificationsOnSwitchPressEnabled()) {
                    ServiceMessageHUD.instance.showMessage(
                        "Press any switch to open notification",
                        ServiceMessageHUD.MessageType.DISAPPEARING
                    )
                }
            }
        })
    }

    /**
     * Determines if pause-on-hold functionality should be enabled based on preferences
     * and scanning settings.
     *
     * @return true if scanning should pause when a switch is held down
     */
    private fun isPauseEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD) ||
                scanSettings.isPauseScanOnSwitchHoldRequired()
    }

    /**
     * Handles switch press events. This is the main entry point for processing
     * switch interactions.
     *
     * @param keyCode The system key code of the pressed switch
     * @return true if the event was handled and should be consumed, false otherwise
     */
    fun onSwitchPressed(keyCode: Int): Boolean {
        val switchEvent = findSwitchEvent(keyCode) ?: return false
        switchEvent.log()

        // Handle notifications if there's a waiting notification
        if (shouldHandleNotification()) {
            notificationManager.handleSwitch()
            return true
        }

        if (handleSwitchPressedRepeat(keyCode)) return true

        return processSwitchPressedActions(switchEvent)
    }

    /**
     * Handles switch release events.
     *
     * @param keyCode The system key code of the released switch
     * @return true if the event was handled and should be consumed, false otherwise
     */
    fun onSwitchReleased(keyCode: Int): Boolean {
        val switchEvent = findSwitchEvent(keyCode) ?: return false
        val absorbedAction = latestAction?.takeIf { it.switchEvent == switchEvent } ?: return true

        processSwitchReleasedActions(switchEvent, absorbedAction)
        return true
    }

    /**
     * Checks if a notification should be handled based on current settings and state.
     *
     * @return true if there's a waiting notification that should be handled
     */
    private fun shouldHandleNotification(): Boolean {
        return scanSettings.isOpenNotificationsOnSwitchPressEnabled() &&
                notificationManager.isNotificationWaiting()
    }

    /**
     * Looks up the switch event configuration for a given key code.
     *
     * @param keyCode The system key code to look up
     * @return The corresponding SwitchEvent or null if not found
     */
    private fun findSwitchEvent(keyCode: Int): SwitchEvent? {
        Log.d("SwitchListener", "Finding switch event for keyCode: $keyCode")
        return switchEventStore.find(keyCode.toString())
    }

    /**
     * Handles repeated switch press actions based on timing and settings.
     *
     * @param keyCode The system key code of the switch
     * @return true if the repeat should be ignored
     */
    private fun handleSwitchPressedRepeat(keyCode: Int): Boolean {
        return if (shouldIgnoreSwitchRepeat(keyCode)) {
            Log.d("SwitchListener", "Ignoring switch repeat: $keyCode")
            true
        } else {
            updateSwitchPressTime(keyCode)
            false
        }
    }

    /**
     * Processes actions associated with a switch press event.
     *
     * @param switchEvent The switch event to process
     * @return true if the event was processed successfully
     */
    private fun processSwitchPressedActions(switchEvent: SwitchEvent): Boolean {
        latestAction = AbsorbedSwitchAction(switchEvent, System.currentTimeMillis())
        val pauseEnabled = isPauseEnabled()

        return when {
            scanningManager.startMoveRepeat(switchEvent.pressAction) -> true
            switchEvent.holdActions.isEmpty() && !pauseEnabled -> {
                handleImmediatePressAction(switchEvent)
                true
            }

            else -> handleLongPressAction(switchEvent, pauseEnabled)
        }
    }

    /**
     * Handles immediate (non-hold) press actions for a switch event.
     *
     * @param switchEvent The switch event to handle
     */
    private fun handleImmediatePressAction(switchEvent: SwitchEvent) {
        if (SelectionHandler.isAutoSelectInProgress()) {
            SelectionHandler.performSelectionAction()
        } else {
            scanningManager.performAction(switchEvent.pressAction)
        }
    }

    /**
     * Handles long press actions for a switch event.
     *
     * @param switchEvent The switch event to handle
     * @param pauseEnabled Whether scanning should pause during the hold
     * @return true if the long press was handled successfully
     */
    private fun handleLongPressAction(switchEvent: SwitchEvent, pauseEnabled: Boolean): Boolean {
        SwitchLongPressHandler.startLongPress(context, switchEvent.holdActions)
        if (pauseEnabled) {
            scanningManager.pauseScanning()
        }
        return true
    }

    /**
     * Processes actions associated with a switch release event.
     *
     * @param switchEvent The switch event being released
     * @param absorbedAction The absorbed action from the initial press
     */
    private fun processSwitchReleasedActions(
        switchEvent: SwitchEvent,
        absorbedAction: AbsorbedSwitchAction
    ) {
        if (scanningManager.stopMoveRepeat()) return
        SwitchLongPressHandler.stopLongPress(scanningManager)

        val timeElapsed = System.currentTimeMillis() - absorbedAction.time
        handleSwitchReleaseActions(switchEvent, timeElapsed)
    }

    /**
     * Handles actions for switch release based on hold time and settings.
     *
     * @param switchEvent The switch event being released
     * @param timeElapsed Time elapsed since the initial press
     */
    private fun handleSwitchReleaseActions(switchEvent: SwitchEvent, timeElapsed: Long) {
        val switchHoldTime =
            preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)
        val pauseEnabled = isPauseEnabled()

        if (pauseEnabled && !SelectionHandler.isAutoSelectInProgress()) {
            scanningManager.resumeScanning()
        }

        when {
            SelectionHandler.isAutoSelectInProgress() &&
                    (switchEvent.holdActions.isNotEmpty() || pauseEnabled) ->
                SelectionHandler.performSelectionAction()

            switchEvent.holdActions.isEmpty() && pauseEnabled ->
                scanningManager.performAction(switchEvent.pressAction)

            switchEvent.holdActions.isNotEmpty() && timeElapsed < switchHoldTime ->
                scanningManager.performAction(switchEvent.pressAction)
        }
    }

    /**
     * Determines if a switch repeat should be ignored based on timing and settings.
     *
     * @param keyCode The system key code of the switch
     * @return true if the repeat should be ignored
     */
    private fun shouldIgnoreSwitchRepeat(keyCode: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        val ignoreRepeat =
            preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT)
        val ignoreRepeatDelay =
            preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY)

        return ignoreRepeat &&
                keyCode == lastSwitchPressedCode &&
                currentTime - lastSwitchPressedTime < ignoreRepeatDelay
    }

    /**
     * Updates the timing information for switch press repeat handling.
     *
     * @param keyCode The system key code of the pressed switch
     */
    private fun updateSwitchPressTime(keyCode: Int) {
        lastSwitchPressedTime = System.currentTimeMillis()
        lastSwitchPressedCode = keyCode
    }

    /**
     * Data class representing an absorbed switch action and its timing information.
     *
     * @property switchEvent The switch event that was absorbed
     * @property time The timestamp when the action was absorbed
     */
    private data class AbsorbedSwitchAction(val switchEvent: SwitchEvent, val time: Long)
}