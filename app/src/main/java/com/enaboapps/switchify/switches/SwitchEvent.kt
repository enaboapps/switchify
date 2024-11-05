package com.enaboapps.switchify.switches

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class SwitchEvent(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("press_action") val pressAction: SwitchAction,
    @SerializedName("hold_actions") val holdActions: List<SwitchAction>
) {
    fun toJson(): String = Gson().toJson(this)

    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "code" to code,
        "press_action" to pressAction.toMap(),
        "hold_actions" to holdActions.map { it.toMap() }
    )

    fun log() {
        println("SwitchEvent: $name, $code, ${pressAction.id}, ${holdActions.joinToString(separator = ";") { it.id.toString() }}")
    }

    fun containsAction(actionId: Int): Boolean {
        return pressAction.id == actionId || holdActions.any { it.id == actionId }
    }

    companion object {
        fun fromJson(json: String): SwitchEvent = Gson().fromJson(json, SwitchEvent::class.java)
    }
}