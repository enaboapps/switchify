package com.enaboapps.switchify.switches

import com.google.gson.annotations.SerializedName

const val SWITCH_EVENT_TYPE_EXTERNAL = "external"
const val SWITCH_EVENT_TYPE_CAMERA = "camera"

data class SwitchEvent(
    @SerializedName("type") var type: String = SWITCH_EVENT_TYPE_EXTERNAL,
    @SerializedName("name") var name: String,
    @SerializedName("code") var code: String,
    @SerializedName("facial_gesture_time") var facialGestureTime: Long = 100L,
    @SerializedName("press_action") var pressAction: SwitchAction,
    @SerializedName("hold_actions") var holdActions: List<SwitchAction>,
    @Transient var isOnDevice: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "type" to (type.takeIf { it.isNotEmpty() } ?: SWITCH_EVENT_TYPE_EXTERNAL),
        "name" to name,
        "code" to code,
        "facial_gesture_time" to (facialGestureTime.takeIf { it > 0 } ?: 100L),
        "press_action" to pressAction.toMap(),
        "hold_actions" to holdActions.map { it.toMap() }
    )

    fun setValuesFromOther(other: SwitchEvent) {
        type = other.type
        name = other.name
        code = other.code
        facialGestureTime = other.facialGestureTime
        pressAction = other.pressAction
        holdActions = other.holdActions
        isOnDevice = other.isOnDevice
    }

    fun log() {
        println(
            "SwitchEvent: $type, $name, $code, $facialGestureTime, ${pressAction.id}, ${
                holdActions.joinToString(
                    separator = ";"
                ) { it.id.toString() }
            }, isOnDevice: $isOnDevice"
        )
    }

    fun containsAction(actionId: Int): Boolean {
        return pressAction.id == actionId || holdActions.any { it.id == actionId }
    }
}