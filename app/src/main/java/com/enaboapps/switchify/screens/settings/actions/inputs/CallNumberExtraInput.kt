package com.enaboapps.switchify.screens.settings.actions.inputs

import androidx.compose.runtime.Composable
import com.enaboapps.switchify.components.TextArea
import com.enaboapps.switchify.service.custom.actions.store.data.ActionExtra

@Composable
fun CallNumberExtraInput(
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    TextArea(
        value = selectedExtra?.numberToCall ?: "",
        onValueChange = { text ->
            onExtraUpdated(
                ActionExtra(
                    numberToCall = text
                )
            )
            val isValid = text.isNotBlank() && text.matches(Regex("^\\d+$"))
            onExtraValidated(isValid)
        },
        label = "Number to Call",
        isError = selectedExtra?.numberToCall.isNullOrBlank() == true,
        supportingText = "Number to call is required"
    )
}
