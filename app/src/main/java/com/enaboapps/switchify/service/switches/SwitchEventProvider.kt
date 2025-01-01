package com.enaboapps.switchify.service.switches

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.switches.SWITCH_EVENT_TYPE_CAMERA
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class SwitchEventProvider(context: Context) {
    private val store = SwitchEventStore(context.applicationContext, true)
    private val cameraSwitchListeners = mutableSetOf<CameraSwitchListener>()
    var hasCameraSwitch = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SwitchEventStore.EVENTS_UPDATED) {
                store.reload()
                checkCameraSwitchAvailability()
                notifyCameraSwitchListeners()
            }
        }
    }

    init {
        LocalBroadcastManager.getInstance(context.applicationContext).registerReceiver(
            receiver,
            IntentFilter(SwitchEventStore.EVENTS_UPDATED)
        )
    }

    fun findExternal(code: String): SwitchEvent? = store.findExternal(code)

    fun findCamera(code: String): SwitchEvent? = store.findCamera(code)

    fun addCameraSwitchListener(listener: CameraSwitchListener) {
        cameraSwitchListeners.add(listener)
    }

    fun removeCameraSwitchListener(listener: CameraSwitchListener) {
        cameraSwitchListeners.remove(listener)
    }

    fun isFacialGestureAssigned(gestureId: String): Boolean {
        return store.getSwitchEvents()
            .any { it.type == SWITCH_EVENT_TYPE_CAMERA && it.isOnDevice && it.code == gestureId } == true
    }

    private fun checkCameraSwitchAvailability() {
        val hasCamera = store.getSwitchEvents()
            .any { it.type == SWITCH_EVENT_TYPE_CAMERA && it.isOnDevice } == true

        if (hasCamera != hasCameraSwitch) {
            hasCameraSwitch = hasCamera
        }
    }

    private fun notifyCameraSwitchListeners() {
        cameraSwitchListeners.forEach { listener ->
            listener.onCameraSwitchAvailabilityChanged(hasCameraSwitch)
        }
    }

    interface CameraSwitchListener {
        fun onCameraSwitchAvailabilityChanged(available: Boolean)
    }
}