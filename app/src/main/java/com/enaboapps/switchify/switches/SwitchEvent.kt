package com.enaboapps.switchify.switches

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

const val SWITCH_EVENT_TYPE_EXTERNAL = "external"
const val SWITCH_EVENT_TYPE_CAMERA = "camera"

data class SwitchEvent(
    @SerializedName("type") val type: String = SWITCH_EVENT_TYPE_EXTERNAL,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("press_action") val pressAction: SwitchAction,
    @SerializedName("hold_actions") val holdActions: List<SwitchAction>,
    var isOnDevice: Boolean = false
) {
    fun toJson(): String = Gson().toJson(this)

    fun toMap(): Map<String, Any> = mapOf(
        "type" to (type.takeIf { it.isNotEmpty() } ?: SWITCH_EVENT_TYPE_EXTERNAL),
        "name" to name,
        "code" to code,
        "press_action" to pressAction.toMap(),
        "hold_actions" to holdActions.map { it.toMap() }
    )

    fun log() {
        println(
            "SwitchEvent: $type, $name, $code, ${pressAction.id}, ${
                holdActions.joinToString(
                    separator = ";"
                ) { it.id.toString() }
            }, isOnDevice: $isOnDevice"
        )
    }

    fun containsAction(actionId: Int): Boolean {
        return pressAction.id == actionId || holdActions.any { it.id == actionId }
    }

    companion object {
        fun fromJson(json: String): SwitchEvent = Gson().fromJson(json, SwitchEvent::class.java)
    }
}