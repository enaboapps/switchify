package com.enaboapps.switchify.screens.settings.switches.models

import android.content.Context
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.*

class AddEditCameraSwitchScreenModel : ViewModel() {
    var name = ""
    val selectedGesture = mutableStateOf<CameraSwitchFacialGesture?>(null)
    val action = mutableStateOf(SwitchAction(SwitchAction.ACTION_SELECT))
    val isValid = mutableStateOf(false)
    val facialGestureTime = mutableLongStateOf(100L)
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
                facialGestureTime.longValue = it.facialGestureTime
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
        facialGestureTime.longValue = newValue
        validate()
    }

    private fun validate() {
        isValid.value = name.isNotBlank() &&
                selectedGesture.value != null &&
                action.value != SwitchAction(SwitchAction.ACTION_NONE)
    }

    fun save(completion: ((Boolean) -> Unit)) {
        val event = SwitchEvent(
            type = SWITCH_EVENT_TYPE_CAMERA,
            name = name.trim(),
            code = selectedGesture.value?.id ?: "",
            facialGestureTime = facialGestureTime.longValue,
            pressAction = action.value,
            holdActions = emptyList()
        )

        if (store.find(event.code) == null) {
            store.add(event) { success ->
                if (success) {
                    completion(true)
                } else {
                    completion(false)
                }
            }
        } else {
            store.update(event) { success ->
                if (success) {
                    completion(true)
                } else {
                    completion(false)
                }
            }
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