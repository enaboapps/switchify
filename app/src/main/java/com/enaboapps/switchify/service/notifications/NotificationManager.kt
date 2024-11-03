package com.enaboapps.switchify.service.notifications

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.atomic.AtomicBoolean

class NotificationManager(private val accessibilityService: AccessibilityService) {

    data class NotificationInfo(
        val title: String?,
        val text: String?,
        val packageName: String,
        val timestamp: Long,
        val notification: Notification
    )

    interface NotificationListener {
        fun onNotificationReceived(notification: NotificationInfo)
    }

    private var listener: NotificationListener? = null
    private var currentNotification: NotificationInfo? = null
    private val handler = Handler(Looper.getMainLooper())
    private val isNotificationActive = AtomicBoolean(false)

    // Configurable timeout duration in milliseconds
    var timeoutDuration: Long = 5000 // Default 5 seconds

    fun setListener(listener: NotificationListener) {
        this.listener = listener
    }

    private val timeoutRunnable = Runnable {
        if (isNotificationActive.get()) {
            isNotificationActive.set(false)
            currentNotification = null
        }
    }

    fun handleAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val notification = event.parcelableData as? Notification ?: return
            val packageName = event.packageName?.toString() ?: return

            val notificationInfo = NotificationInfo(
                title = notification.extras.getString(Notification.EXTRA_TITLE),
                text = notification.extras.getString(Notification.EXTRA_TEXT),
                packageName = packageName,
                timestamp = notification.`when`,
                notification = notification
            )

            // Store current notification and start timeout
            currentNotification = notificationInfo
            startNotificationTimeout()

            listener?.onNotificationReceived(notificationInfo)
        }
    }

    private fun startNotificationTimeout() {
        isNotificationActive.set(true)
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, timeoutDuration)
    }

    fun cancelTimeout() {
        handler.removeCallbacks(timeoutRunnable)
        isNotificationActive.set(false)
        currentNotification = null
    }

    fun isNotificationWaiting(): Boolean {
        return isNotificationActive.get()
    }

    fun getCurrentNotification(): NotificationInfo? {
        return if (isNotificationActive.get()) currentNotification else null
    }

    fun openNotification(notification: NotificationInfo) {
        try {
            // Try using PendingIntent first
            notification.notification.contentIntent?.send()
            cancelTimeout() // Cancel timeout after successful opening
        } catch (e: Exception) {
            println("Failed to open notification using PendingIntent: ${e.message}")
            try {
                // Fallback to accessibility action if PendingIntent fails
                val rootNode = accessibilityService.rootInActiveWindow
                rootNode?.findAccessibilityNodeInfosByText(notification.title)?.firstOrNull()
                    ?.let { node ->
                        val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (success) {
                            cancelTimeout() // Cancel timeout after successful opening
                        } else {
                            println("Failed to open notification")
                        }
                    }
            } catch (e: Exception) {
                println("Failed to open notification: ${e.message}")
            }
        }
    }

    fun handleSwitch() {
        getCurrentNotification()?.let { notification ->
            openNotification(notification)
        }
    }
}