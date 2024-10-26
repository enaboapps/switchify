package com.enaboapps.switchify.screens.settings.switches.models

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class EditSwitchScreenModel(
    private val code: String,
    private val store: SwitchEventStore
) : ViewModel() {

    val name = MutableLiveData("")
    val pressAction = MutableLiveData(SwitchAction(SwitchAction.ACTION_SELECT))
    val longPressActions = MutableLiveData<List<SwitchAction>>(emptyList())
    val isValid = MutableLiveData(false)
    val allowLongPress = MutableLiveData(true)

    init {
        val event = store.find(code)
        name.value = event?.name
        pressAction.value = event?.pressAction
        longPressActions.value =
            event?.holdActions ?: listOf() // Initialize with multiple long press actions
        isValid.value = store.validateSwitchEvent(event!!)
    }

    fun save(completion: () -> Unit) {
        val event = buildSwitchEvent()
        store.update(event)
        completion()
    }

    fun setPressAction(action: SwitchAction, context: Context) {
        val settings = ScanSettings(context)
        if (settings.isMoveRepeatEnabled() && (action.id == SwitchAction.ACTION_MOVE_TO_NEXT_ITEM || action.id == SwitchAction.ACTION_MOVE_TO_PREVIOUS_ITEM)) {
            allowLongPress.value = false
        }
        isValid.value = store.validateSwitchEvent(buildSwitchEvent())
    }

    private fun buildSwitchEvent(): SwitchEvent {
        return SwitchEvent(
            name = name.value!!,
            code = code,
            pressAction = pressAction.value!!,
            holdActions = longPressActions.value!! // Save the list of long press actions
        )
    }

    fun updateLongPressAction(newAction: SwitchAction, index: Int) {
        longPressActions.value?.let { actions ->
            val updatedActions = actions.toMutableList()
            updatedActions[index] = newAction
            longPressActions.value = updatedActions
        }
        isValid.value = store.validateSwitchEvent(buildSwitchEvent())
    }

    fun delete(completion: () -> Unit) {
        val event = store.find(code)
        event?.let {
            store.remove(it)
            completion()
        }
    }

    fun addLongPressAction(action: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        currentActions.add(action)
        longPressActions.value = currentActions
        isValid.value = store.validateSwitchEvent(buildSwitchEvent())
    }

    fun removeLongPressAction(index: Int) {
        longPressActions.value?.let { actions ->
            val updatedActions = actions.toMutableList()
            updatedActions.removeAt(index)
            longPressActions.value = updatedActions
        }
        isValid.value = store.validateSwitchEvent(buildSwitchEvent())
    }
}