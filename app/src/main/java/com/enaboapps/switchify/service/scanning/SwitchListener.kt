package com.enaboapps.switchify.service.scanning

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore
import java.util.Timer
import java.util.TimerTask

// SwitchListener class to handle switch events
class SwitchListener(
    private val context: Context,
    private val scanningManager: ScanningManager
) {

    // PreferenceManager to access user preferences
    private val preferenceManager = PreferenceManager(context)

    // Store for switch events
    private val switchEventStore = SwitchEventStore(context)

    // Variable to track the latest absorbed switch action
    private var latestAction: AbsorbedSwitchAction? = null

    // Timer for tracking switch hold duration
    private var switchHoldTimer: Timer? = null

    // Variable for tracking if long press action is performed
    private var longPressPerformed = false

    // Variables for ignoring switch repeat
    private var lastSwitchPressedTime: Long = 0
    private var lastSwitchPressedCode: Int = 0


    // Function to start the switch hold timer
    private fun startSwitchHoldTimer() {
        switchHoldTimer = Timer()
        switchHoldTimer?.schedule(
            object : TimerTask() {
                override fun run() {
                    // Cancel the timer when it runs out
                    switchHoldTimer?.cancel()
                    switchHoldTimer = null

                    // Perform long press action if available
                    latestAction?.let {
                        scanningManager.performAction(it.switchEvent.longPressAction)
                        longPressPerformed = true
                    }
                }
            },
            preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)
        )
    }

    /**
     * Called when a switch is pressed
     * @param keyCode the key code of the switch event
     * @return true if the event should be absorbed, false otherwise
     */
    fun onSwitchPressed(keyCode: Int): Boolean {
        val switchEvent = switchEventStore.find(keyCode.toString())
        Log.d("SwitchListener", "onSwitchPressed: $keyCode")
        return switchEvent?.let {
            if (shouldIgnoreSwitchRepeat(keyCode)) {
                return false // Absorb the event, but don't perform any action
            }

            latestAction = AbsorbedSwitchAction(it, System.currentTimeMillis())

            // Handle immediate press action or start hold timer for long press
            if (it.longPressAction.id == SwitchAction.Actions.ACTION_NONE) {
                scanningManager.performAction(it.pressAction)
            } else {
                startSwitchHoldTimer()
                // Pause scanning if the setting is enabled
                if (preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD)) {
                    scanningManager.pauseScanning()
                }
            }
            false
        } ?: true
    }

    /**
     * Called when a switch is released
     * @param keyCode the key code of the switch event
     * @return true if the event should be absorbed, false otherwise
     */
    fun onSwitchReleased(keyCode: Int): Boolean {
        val switchEvent = switchEventStore.find(keyCode.toString())
        Log.d("SwitchListener", "onSwitchReleased: $keyCode")
        return switchEvent?.let { event ->
            latestAction?.takeIf { it.switchEvent == event }?.let {
                // Check ignore repeat setting
                if (shouldIgnoreSwitchRepeat(keyCode)) {
                    switchHoldTimer?.cancel() // Cancel any running timer
                    return false // Absorb the event, but don't perform any action
                }

                val timeElapsed = System.currentTimeMillis() - it.time
                val switchHoldTime =
                    preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)

                // Toggle swipe lock if time elapsed is greater than hold time
                if (timeElapsed > switchHoldTime && GestureManager.getInstance()
                        .isSwipeLockEnabled()
                ) {
                    GestureManager.getInstance().toggleSwipeLock()
                    longPressPerformed = false
                    return true
                }

                // Resume scanning if the setting is enabled
                if (preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD)) {
                    scanningManager.resumeScanning()
                }

                // Check if long press action is performed
                if (longPressPerformed) {
                    longPressPerformed = false
                    return true // Absorb the event
                }

                if (event.longPressAction.id != SwitchAction.Actions.ACTION_NONE) {
                    // Perform press action if time elapsed is less than hold time
                    if (timeElapsed < switchHoldTime) {
                        scanningManager.performAction(event.pressAction)
                    }
                }

                // Cancel any running timer
                switchHoldTimer?.cancel()
            }
            false
        } ?: true
    }

    /**
     * Check if the switch event should be ignored based on the repeat delay setting
     * @param keyCode the key code of the switch event
     * @return true if the event should be ignored, false otherwise
     */
    private fun shouldIgnoreSwitchRepeat(keyCode: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        val ignoreRepeat =
            preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT)
        val ignoreRepeatDelay =
            preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY)

        return if (ignoreRepeat && keyCode == lastSwitchPressedCode && currentTime - lastSwitchPressedTime < ignoreRepeatDelay) {
            Log.d("SwitchListener", "Ignoring switch repeat: $keyCode")
            true
        } else {
            lastSwitchPressedTime = currentTime
            lastSwitchPressedCode = keyCode
            false
        }
    }

    /**
     * Data class to hold the absorbed switch action and the time it was absorbed
     * @param switchEvent the absorbed switch event
     * @param time the time the switch event was absorbed
     */
    private data class AbsorbedSwitchAction(val switchEvent: SwitchEvent, val time: Long)
}