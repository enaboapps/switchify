package com.enaboapps.switchify.service.switches

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.selection.SelectionHandler
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

/**
 * Class to handle switch events.
 *
 * @property context Context of the application.
 * @property scanningManager Manager to handle scanning actions.
 */
class SwitchListener(
    private val context: Context,
    private val scanningManager: ScanningManager
) {

    private val preferenceManager = PreferenceManager(context)
    private val switchEventStore = SwitchEventStore(context)
    private var latestAction: AbsorbedSwitchAction? = null
    private var lastSwitchPressedTime: Long = 0
    private var lastSwitchPressedCode: Int = 0

    /**
     * Receives broadcasts from the SwitchEventStore to update the switch events.
     */
    private val switchEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            switchEventStore.reload()
        }
    }

    init {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            switchEventReceiver,
            IntentFilter(SwitchEventStore.EVENTS_UPDATED)
        )
    }

    /**
     * Checks if the pause on switch hold feature is enabled.
     * If the pause on switch hold feature is enabled, it returns true.
     * If the pause on switch hold feature is not enabled, it returns true if the scan settings require it.
     *
     * @return True if pause on switch hold is enabled, false otherwise.
     */
    private fun isPauseEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD) ||
                ScanSettings(context).isPauseScanOnSwitchHoldRequired()
    }

    /**
     * Called when a switch is pressed.
     *
     * @param keyCode The key code of the switch event.
     * @return True if the event should be absorbed, false otherwise.
     */
    fun onSwitchPressed(keyCode: Int): Boolean {
        val switchEvent = findSwitchEvent(keyCode) ?: return false
        switchEvent.log()
        if (handleSwitchPressedRepeat(keyCode)) return true

        return processSwitchPressedActions(switchEvent)
    }

    /**
     * Called when a switch is released.
     *
     * @param keyCode The key code of the switch event.
     * @return True if the event should be absorbed, false otherwise.
     */
    fun onSwitchReleased(keyCode: Int): Boolean {
        val switchEvent = findSwitchEvent(keyCode) ?: return false
        val absorbedAction = latestAction?.takeIf { it.switchEvent == switchEvent } ?: return true

        processSwitchReleasedActions(switchEvent, absorbedAction)
        return true
    }

    /**
     * Finds the switch event corresponding to the given key code.
     *
     * @param keyCode The key code of the switch event.
     * @return The corresponding SwitchEvent, or null if not found.
     */
    private fun findSwitchEvent(keyCode: Int): SwitchEvent? {
        Log.d("SwitchListener", "Finding switch event for keyCode: $keyCode")
        return switchEventStore.find(keyCode.toString())
    }

    /**
     * Handles repeated switch press actions.
     *
     * @param keyCode The key code of the switch event.
     * @return True if the repeat should be ignored, false otherwise.
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
     * Processes actions for switch press events.
     *
     * @param switchEvent The switch event to process.
     * @return True if the event was successfully processed, false otherwise.
     */
    private fun processSwitchPressedActions(switchEvent: SwitchEvent): Boolean {
        latestAction = AbsorbedSwitchAction(switchEvent, System.currentTimeMillis())
        val pauseEnabled = isPauseEnabled()

        return when {
            scanningManager.startMoveRepeat(switchEvent.pressAction) -> {
                true
            }

            switchEvent.holdActions.isEmpty() && !pauseEnabled -> {
                handleImmediatePressAction(switchEvent)
                true
            }

            else -> handleLongPressAction(switchEvent, pauseEnabled)
        }
    }

    /**
     * Handles immediate press actions for switch events.
     *
     * @param switchEvent The switch event to handle.
     */
    private fun handleImmediatePressAction(switchEvent: SwitchEvent) {
        if (SelectionHandler.isAutoSelectInProgress()) SelectionHandler.performSelectionAction()
        else scanningManager.performAction(switchEvent.pressAction)
    }

    /**
     * Handles long press actions for switch events.
     *
     * @param switchEvent The switch event to handle.
     * @param pauseEnabled Whether pausing is enabled.
     * @return True if the long press action was handled successfully, false otherwise.
     */
    private fun handleLongPressAction(switchEvent: SwitchEvent, pauseEnabled: Boolean): Boolean {
        SwitchLongPressHandler.startLongPress(context, switchEvent.holdActions)
        if (pauseEnabled) {
            scanningManager.pauseScanning()
        }
        return true
    }

    /**
     * Processes actions for switch release events.
     *
     * @param switchEvent The switch event to process.
     * @param absorbedAction The absorbed switch action.
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
     * Handles actions for switch release events based on their state.
     *
     * @param switchEvent The switch event to handle.
     * @param timeElapsed The time elapsed since the switch was pressed.
     */
    private fun handleSwitchReleaseActions(switchEvent: SwitchEvent, timeElapsed: Long) {
        val switchHoldTime =
            preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)
        val pauseEnabled = isPauseEnabled()

        if (pauseEnabled && !SelectionHandler.isAutoSelectInProgress()) scanningManager.resumeScanning()

        when {
            SelectionHandler.isAutoSelectInProgress() && (switchEvent.holdActions.isNotEmpty() || pauseEnabled) -> SelectionHandler.performSelectionAction()
            switchEvent.holdActions.isEmpty() && pauseEnabled -> scanningManager.performAction(
                switchEvent.pressAction
            )

            switchEvent.holdActions.isNotEmpty() && timeElapsed < switchHoldTime -> scanningManager.performAction(
                switchEvent.pressAction
            )
        }
    }

    /**
     * Determines if a switch repeat should be ignored based on the settings.
     *
     * @param keyCode The key code of the switch event.
     * @return True if the repeat should be ignored, false otherwise.
     */
    private fun shouldIgnoreSwitchRepeat(keyCode: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        val ignoreRepeat =
            preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT)
        val ignoreRepeatDelay =
            preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY)

        return ignoreRepeat && keyCode == lastSwitchPressedCode && currentTime - lastSwitchPressedTime < ignoreRepeatDelay
    }

    /**
     * Updates the time of the last switch press.
     *
     * @param keyCode The key code of the switch event.
     */
    private fun updateSwitchPressTime(keyCode: Int) {
        lastSwitchPressedTime = System.currentTimeMillis()
        lastSwitchPressedCode = keyCode
    }

    /**
     * Data class to hold the absorbed switch action and the time it was absorbed.
     *
     * @property switchEvent The absorbed switch event.
     * @property time The time the switch event was absorbed.
     */
    private data class AbsorbedSwitchAction(val switchEvent: SwitchEvent, val time: Long)
}