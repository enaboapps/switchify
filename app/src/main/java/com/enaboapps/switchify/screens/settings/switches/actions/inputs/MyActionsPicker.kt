package com.enaboapps.switchify.screens.settings.switches.actions.inputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.enaboapps.switchify.service.custom.actions.store.Action
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchActionExtra
import com.enaboapps.switchify.widgets.Picker

@Composable
fun MyActionsPicker(
    currentAction: SwitchAction,
    onChange: (SwitchAction) -> Unit
) {
    val context = LocalContext.current
    val actionStore = ActionStore(context)
    val actions = actionStore.getActions()
    val selectedAction = remember { mutableStateOf<Action?>(null) }

    val createAction: (String, String) -> SwitchAction = { id, text ->
        SwitchAction(
            id = SwitchAction.ACTION_PERFORM_USER_ACTION,
            extra = SwitchActionExtra(
                myActionsId = id,
                myActionName = text
            )
        )
    }

    LaunchedEffect(Unit) {
        currentAction.extra?.myActionsId?.let { id ->
            selectedAction.value = actions.firstOrNull { it.id == id }
        }
    }

    Picker(
        title = "Select My Action",
        selectedItem = currentAction,
        items = actions.map { action ->
            createAction(action.id, action.text)
        },
        onItemSelected = { newAction ->
            selectedAction.value = actions.firstOrNull { it.id == newAction.extra?.myActionsId }
            onChange(newAction)
        },
        itemToString = { selectedAction.value?.text ?: "" },
        itemDescription = { "Perform this action" }
    )
}
