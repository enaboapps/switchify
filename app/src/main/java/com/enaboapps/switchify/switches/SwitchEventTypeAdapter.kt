package com.enaboapps.switchify.switches

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SwitchEventTypeAdapter : JsonDeserializer<SwitchEvent> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SwitchEvent {
        val jsonObject = json.asJsonObject

        // Get type with default value if missing
        val type = if (jsonObject.has("type")) {
            jsonObject.get("type").asString
        } else {
            SWITCH_EVENT_TYPE_EXTERNAL
        }

        // Get required fields
        val name = jsonObject.get("name").asString
        val code = jsonObject.get("code").asString
        val pressAction = context.deserialize<SwitchAction>(
            jsonObject.get("press_action"),
            SwitchAction::class.java
        )
        val holdActions = context.deserialize<List<SwitchAction>>(
            jsonObject.get("hold_actions"),
            object : TypeToken<List<SwitchAction>>() {}.type
        )

        return SwitchEvent(
            type = type,
            name = name,
            code = code,
            pressAction = pressAction,
            holdActions = holdActions
        )
    }
}