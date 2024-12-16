package com.enaboapps.switchify.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.lockscreen.LockScreenView
import com.enaboapps.switchify.service.methods.nodes.NodeExaminer
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.selection.SelectionHandler
import com.enaboapps.switchify.service.switches.SwitchListener
import com.enaboapps.switchify.service.utils.KeyboardBridge
import com.enaboapps.switchify.service.utils.ScreenWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * This is the main service class for the Switchify application.
 * It extends the AccessibilityService class to provide accessibility features.
 */
class SwitchifyAccessibilityService : AccessibilityService() {

    private lateinit var scanningManager: ScanningManager
    private lateinit var switchListener: SwitchListener
    private lateinit var screenWatcher: ScreenWatcher
    private lateinit var lockScreenView: LockScreenView
    private lateinit var scanSettings: ScanSettings
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * This method is called when an AccessibilityEvent is fired.
     * It updates the nodes in the active window and the keyboard state.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        rootInActiveWindow?.let { rootNode ->
            serviceScope.launch {
                NodeExaminer.findNodes(rootNode, this@SwitchifyAccessibilityService, this)
            }
        }
        KeyboardBridge.updateKeyboardState(windows, this)
    }

    override fun onInterrupt() {}

    /**
     * This method is called when the service is connected.
     * It sets up the scanning manager, switch listener, screen watcher, lock screen view, gesture manager, and auto selection handler.
     * It also finds nodes in the active window and updates the keyboard state.
     */
    override fun onServiceConnected() {
        super.onServiceConnected()

        ScanMethod.preferenceManager = PreferenceManager(this.applicationContext)

        scanningManager = ScanningManager(this, this)
        scanningManager.setup()

        lockScreenView = LockScreenView(this)
        lockScreenView.setup(this)

        screenWatcher = ScreenWatcher(
            onScreenWake = { lockScreenView.show() },
            onScreenSleep = {
                switchListener.reset()
                scanningManager.reset()
                lockScreenView.hide()
            },
            onOrientationChanged = { scanningManager.reset() }
        )
        screenWatcher.register(this)

        scanSettings = ScanSettings(this)

        switchListener = SwitchListener(this, scanningManager)

        GestureManager.getInstance().setup(this)
        SelectionHandler.init(this)

        rootInActiveWindow?.let { rootNode ->
            serviceScope.launch {
                NodeExaminer.findNodes(rootNode, this@SwitchifyAccessibilityService, this)
            }
        }
        KeyboardBridge.updateKeyboardState(windows, this)

        // Update the NodeScanner with the current layout info
        serviceScope.launch {
            NodeExaminer.observeNodes().collect { nodes ->
                scanningManager.updateNodes(nodes)
            }
        }

        // Show the lock screen when the service starts
        lockScreenView.show()
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        event?.let { handleSwitchEvent(it) }
        return true
    }

    /**
     * This method handles switch press events.
     * It performs different actions based on the type of the key event.
     */
    private fun handleSwitchEvent(event: KeyEvent): Boolean {
        return when (event.action) {
            KeyEvent.ACTION_DOWN -> switchListener.onSwitchPressed(event.keyCode)
            KeyEvent.ACTION_UP -> switchListener.onSwitchReleased(event.keyCode)
            else -> false
        }
    }
}
