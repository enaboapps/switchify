package com.enaboapps.switchify.screens.settings.switches.models

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchAction.Companion.ACTION_MOVE_TO_NEXT_ITEM
import com.enaboapps.switchify.switches.SwitchAction.Companion.ACTION_MOVE_TO_PREVIOUS_ITEM
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class AddEditSwitchScreenModel(
    private val store: SwitchEventStore,
    private val code: String? = null
) : ViewModel() {

    companion object {
        private const val TAG = "AddEditSwitchScreenModel"
    }

    val name = MutableLiveData(
        if (code == null) "Switch ${store.getCount() + 1}" else store.find(code)?.name ?: ""
    )

    val switchCaptured = MutableLiveData(code != null)

    val shouldSave = MutableLiveData(code != null)
    val isValid = MutableLiveData(false)
    val allowLongPress = MutableLiveData(true)
    val refreshingLongPressActions = MutableLiveData(false)

    // Actions for press and long press
    val pressAction = MutableLiveData(
        if (code == null) SwitchAction(SwitchAction.ACTION_SELECT) else store.find(code)?.pressAction
    )
    val longPressActions = MutableLiveData(
        if (code == null) emptyList() else store.find(code)?.holdActions ?: emptyList()
    )

    fun processKeyCode(key: Key, context: Context) {
        Log.d(TAG, "processKeyCode: ${key.nativeKeyCode}")

        // If switch already exists, don't save and show toast
        if (store.find(key.nativeKeyCode.toString()) != null) {
            shouldSave.value = false
            Toast.makeText(context, "Switch already exists", Toast.LENGTH_SHORT).show()
            return
        }

        validate(key.nativeKeyCode.toString())
        shouldSave.value = true
        switchCaptured.value = true
    }

    fun addLongPressAction(action: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        currentActions.add(action)
        longPressActions.value = currentActions
        validate()
    }

    fun removeLongPressAction(index: Int) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        currentActions.removeAt(index)
        longPressActions.value = currentActions
        validate()
        refreshLongPressActions()
    }

    fun refreshLongPressActions() {
        refreshingLongPressActions.value = true
        Handler(Looper.getMainLooper()).postDelayed({
            refreshingLongPressActions.value = false
        }, 300)
    }

    fun updateLongPressAction(oldAction: SwitchAction, newAction: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        val index = currentActions.indexOf(oldAction)
        if (index != -1) {
            currentActions[index] = newAction
            longPressActions.value = currentActions
        }
        validate()
    }

    fun setPressAction(action: SwitchAction, context: Context) {
        pressAction.value = action
        updateAllowLongPress(context)
        validate()
    }

    fun updateAllowLongPress(context: Context) {
        val settings = ScanSettings(context)
        val next = ACTION_MOVE_TO_NEXT_ITEM
        val previous = ACTION_MOVE_TO_PREVIOUS_ITEM
        var pressAction = pressAction.value
        val isMoveRepeat = settings.isMoveRepeatEnabled()
        val isMoveAction = pressAction?.id == next || pressAction?.id == previous
        allowLongPress.value = !(isMoveRepeat && isMoveAction)
        println("Allow long press: ${allowLongPress.value}, isMoveRepeat: $isMoveRepeat, isMoveAction: $isMoveAction")
    }

    fun validate(newCode: String? = code) {
        isValid.value = store.validateSwitchEvent(buildSwitchEvent(newCode))
    }

    private fun buildSwitchEvent(newCode: String? = code): SwitchEvent {
        return SwitchEvent(
            name = name.value!!,
            code = newCode ?: "",
            pressAction = pressAction.value!!,
            holdActions = longPressActions.value!!
        )
    }

    fun save() {
        if (shouldSave.value == true) {
            val event = buildSwitchEvent()
            if (store.find(event.code) == null) {
                store.add(event)
            } else {
                store.update(event)
                shouldSave.value = false
            }
        }
    }

    fun delete(completion: () -> Unit) {
        val event = store.find(code ?: "")
        event?.let {
            store.remove(it)
            completion()
        }
    }
}
