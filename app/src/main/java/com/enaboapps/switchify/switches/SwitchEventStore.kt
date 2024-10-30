package com.enaboapps.switchify.switches

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class SwitchEventStore(private val context: Context) {
    private val switchEvents = mutableSetOf<SwitchEvent>()
    private val fileName = "switch_events.json"
    private val file: File
        get() = File(context.applicationContext.filesDir, fileName)
    private val gson = Gson()

    companion object {
        const val EVENTS_UPDATED = "com.enaboapps.switchify.EVENTS_UPDATED"
    }

    init {
        readFile()
    }

    fun reload() {
        readFile()
    }

    fun add(switchEvent: SwitchEvent) {
        if (switchEvents.add(switchEvent)) {
            saveToFile()
        }
    }

    fun update(switchEvent: SwitchEvent) {
        switchEvents.find { it.code == switchEvent.code }?.let {
            switchEvents.remove(it)
            switchEvents.add(switchEvent)
            saveToFile()
        }
    }

    fun remove(switchEvent: SwitchEvent) {
        if (switchEvents.remove(switchEvent)) {
            saveToFile()
        }
    }

    fun find(code: String): SwitchEvent? =
        switchEvents.find { it.code == code }?.also {
            Log.d("SwitchEventStore", "Found switch event for code $code")
        } ?: run {
            Log.d("SwitchEventStore", "No switch event found for code $code")
            null
        }

    fun getCount(): Int = switchEvents.size

    fun getSwitchEvents(): Set<SwitchEvent> = switchEvents.toSet()

    fun validateSwitchEvent(switchEvent: SwitchEvent): Boolean {
        val hasName = switchEvent.name.isNotBlank()
        val hasCode = switchEvent.code.isNotBlank()

        if (!validateExtra(switchEvent.pressAction.id, switchEvent.pressAction.extra)) {
            println("Press action extra is invalid")
            return false
        }

        for (holdAction in switchEvent.holdActions) {
            if (!validateExtra(holdAction.id, holdAction.extra)) {
                println("Hold action extra is invalid")
                return false
            }
        }

        println("Switch event validation - hasName: $hasName, hasCode: $hasCode")

        return hasName && hasCode
    }

    private fun validateExtra(type: Int, extra: SwitchActionExtra?): Boolean {
        return when (type) {
            SwitchAction.ACTION_PERFORM_USER_ACTION -> {
                extra != null && extra.myActionsId != null && extra.myActionName != null
            }

            else -> true
        }
    }

    private fun readFile() {
        if (file.exists()) {
            try {
                val type = object : TypeToken<Set<SwitchEvent>>() {}.type
                val events: Set<SwitchEvent> = gson.fromJson(file.readText(), type)
                switchEvents.clear()
                switchEvents.addAll(events)
            } catch (e: Exception) {
                Log.e("SwitchEventStore", "Error reading from file", e)
                deleteFile()
                Toast.makeText(
                    context,
                    "Error reading from file. Please reconfigure your switches.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d("SwitchEventStore", "File does not exist")
        }
    }

    private fun deleteFile() {
        if (file.exists()) {
            file.delete()
        }
    }

    private fun saveToFile() {
        try {
            file.writeText(gson.toJson(switchEvents))
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(EVENTS_UPDATED))
        } catch (e: Exception) {
            Log.e("SwitchEventStore", "Error writing to file", e)
        }
    }

    fun isConfigInvalid(): String? {
        val preferenceManager = PreferenceManager(context)
        var mode =
            ScanMode(preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_SCAN_MODE))
        if (mode.id.isEmpty()) {
            mode = ScanMode(ScanMode.Modes.MODE_AUTO)
        }
        val containsSelect = switchEvents.any { it.containsAction(SwitchAction.ACTION_SELECT) }
        val containsNext =
            switchEvents.any { it.containsAction(SwitchAction.ACTION_MOVE_TO_NEXT_ITEM) }
        val containsPrevious =
            switchEvents.any { it.containsAction(SwitchAction.ACTION_MOVE_TO_PREVIOUS_ITEM) }

        return when (mode.id) {
            ScanMode.Modes.MODE_AUTO ->
                if (containsSelect) null
                else "At least one switch must be configured to the select action."

            ScanMode.Modes.MODE_MANUAL ->
                if (containsSelect && containsNext && containsPrevious) null
                else "At least one switch must be configured to the next, previous, and select actions."

            else -> null
        }
    }
}
