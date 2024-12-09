package com.enaboapps.switchify.screens.settings.actions.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enaboapps.switchify.components.TextArea
import com.enaboapps.switchify.service.custom.actions.store.data.ActionExtra

@Composable
fun SendTextExtraInput(
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    Column {
        TextArea(
            value = selectedExtra?.numberToSend ?: "",
            onValueChange = { text ->
                onExtraUpdated(
                    ActionExtra(
                        numberToSend = text,
                        message = selectedExtra?.message ?: ""
                    )
                )
                val isValid = text.isNotBlank() && text.matches(Regex("^\\d+$"))
                onExtraValidated(isValid)
            },
            label = "Number to Send Text",
            isError = selectedExtra?.numberToSend.isNullOrBlank() == true,
            supportingText = "Number to send text is required"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextArea(
            value = selectedExtra?.message ?: "",
            onValueChange = { text ->
                onExtraUpdated(
                    ActionExtra(
                        numberToSend = selectedExtra?.numberToSend ?: "",
                        message = text
                    )
                )
                onExtraValidated(true)
            },
            label = "Message (Optional)"
        )
    }
}
