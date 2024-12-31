package com.enaboapps.switchify.service.switches

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.switches.SWITCH_EVENT_TYPE_CAMERA
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

object SwitchEventProvider {
    @SuppressLint("StaticFieldLeak")
    private var store: SwitchEventStore? = null
    private var applicationContext: Context? = null
    private val cameraSwitchListeners = mutableSetOf<CameraSwitchListener>()
    var hasCameraSwitch = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SwitchEventStore.EVENTS_UPDATED) {
                store?.reload()
                checkCameraSwitchAvailability()
                notifyCameraSwitchListeners()
            }
        }
    }

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        store = SwitchEventStore(context.applicationContext)

        applicationContext?.let { appContext ->
            LocalBroadcastManager.getInstance(appContext).registerReceiver(
                receiver,
                IntentFilter(SwitchEventStore.EVENTS_UPDATED)
            )
        }
        checkCameraSwitchAvailability()
    }

    fun cleanup() {
        applicationContext?.let { context ->
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
        applicationContext = null
        store = null
        cameraSwitchListeners.clear()
    }

    fun findExternal(code: String): SwitchEvent? = store?.findExternal(code)

    fun findCamera(code: String): SwitchEvent? = store?.findCamera(code)

    fun getAll(): Set<SwitchEvent> = store?.getSwitchEvents() ?: emptySet()

    fun addCameraSwitchListener(listener: CameraSwitchListener) {
        cameraSwitchListeners.add(listener)
    }

    fun removeCameraSwitchListener(listener: CameraSwitchListener) {
        cameraSwitchListeners.remove(listener)
    }

    fun isFacialGestureAssigned(gestureId: String): Boolean {
        return store?.getSwitchEvents()
            ?.any { it.type == SWITCH_EVENT_TYPE_CAMERA && it.isOnDevice && it.code == gestureId } == true
    }

    private fun checkCameraSwitchAvailability() {
        val hasCamera = store?.getSwitchEvents()
            ?.any { it.type == SWITCH_EVENT_TYPE_CAMERA && it.isOnDevice } == true

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