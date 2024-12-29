package com.enaboapps.switchify.screens.settings.switches.models

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.*

class AddEditCameraSwitchScreenModel : ViewModel() {
    var name = ""
    val selectedGesture = mutableStateOf<CameraSwitchFacialGesture?>(null)
    val action = mutableStateOf(SwitchAction(SwitchAction.ACTION_SELECT))
    val isValid = mutableStateOf(false)
    val facialGestureTime = mutableStateOf(100L)
    val showDeleteConfirmation = mutableStateOf(false)

    private lateinit var store: SwitchEventStore
    private var code: String? = null

    fun init(code: String?, context: Context) {
        store = SwitchEventStore(context)
        this.code = code

        if (code != null) {
            val event = store.find(code)
            event?.let {
                name = it.name
                selectedGesture.value = CameraSwitchFacialGesture(it.code)
                action.value = it.pressAction
                facialGestureTime.value = it.facialGestureTime
            }
        } else {
            name = "Camera Switch ${store.getCount() + 1}"
        }
        validate()
    }

    fun updateName(newName: String) {
        name = newName
        validate()
    }

    fun setGesture(gesture: CameraSwitchFacialGesture) {
        selectedGesture.value = gesture
        validate()
    }

    fun setAction(newAction: SwitchAction) {
        action.value = newAction
        validate()
    }

    fun setFacialGestureTime(newValue: Long) {
        facialGestureTime.value = newValue
        validate()
    }

    private fun validate() {
        isValid.value = name.isNotBlank() &&
                selectedGesture.value != null &&
                action.value != SwitchAction(SwitchAction.ACTION_NONE)
    }

    fun save() {
        val event = SwitchEvent(
            type = SWITCH_EVENT_TYPE_CAMERA,
            name = name.trim(),
            code = selectedGesture.value?.id ?: "",
            facialGestureTime = facialGestureTime.value,
            pressAction = action.value,
            holdActions = emptyList()
        )

        if (store.find(event.code) == null) {
            store.add(event)
        } else {
            store.update(event)
        }
    }

    fun delete(completion: (Boolean) -> Unit) {
        val event = store.find(code ?: "")
        event?.let {
            store.remove(it) { success ->
                if (success) {
                    completion(true)
                } else {
                    completion(false)
                }
            }
        }
    }
} 