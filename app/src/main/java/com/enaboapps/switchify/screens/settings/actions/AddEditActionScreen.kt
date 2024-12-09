package com.enaboapps.switchify.screens.settings.actions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.components.FullWidthButton
import com.enaboapps.switchify.components.NavBar
import com.enaboapps.switchify.components.Picker
import com.enaboapps.switchify.components.TextArea
import com.enaboapps.switchify.screens.settings.actions.inputs.AppLaunchExtraInput
import com.enaboapps.switchify.screens.settings.actions.inputs.CallNumberExtraInput
import com.enaboapps.switchify.screens.settings.actions.inputs.CopyTextExtraInput
import com.enaboapps.switchify.screens.settings.actions.inputs.OpenLinkExtraInput
import com.enaboapps.switchify.screens.settings.actions.inputs.SendEmailExtraInput
import com.enaboapps.switchify.screens.settings.actions.inputs.SendTextExtraInput
import com.enaboapps.switchify.service.custom.actions.ActionPerformer
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_CALL_NUMBER
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_COPY_TEXT_TO_CLIPBOARD
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_OPEN_APP
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_OPEN_LINK
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_SEND_EMAIL
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_SEND_TEXT
import com.enaboapps.switchify.service.custom.actions.store.data.ActionExtra
import com.enaboapps.switchify.service.custom.actions.store.data.getActionDescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActionScreen(navController: NavController, actionId: String? = null) {
    val context = LocalContext.current
    val actionStore = remember { ActionStore(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = actionId != null
    val screenTitle = if (isEditMode) "Edit Action" else "Add Action"

    val availableActions = remember { actionStore.getAvailableActions() }

    var selectedAction by remember { mutableStateOf(availableActions.first()) }
    var selectedExtra by remember { mutableStateOf<ActionExtra?>(null) }
    var actionText by remember { mutableStateOf("") }
    var extraValid by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing action data if in edit mode
    LaunchedEffect(actionId) {
        if (isEditMode) {
            actionStore.getAction(actionId)?.let { action ->
                actionText = action.text
                selectedAction = action.action
                selectedExtra = action.extra
                println("Loaded action: $action")
            }
        }
    }

    val actionPerformer = remember { ActionPerformer(context) }

    val buttonsEnabled = remember(actionText, selectedAction, selectedExtra, extraValid) {
        actionText.isNotBlank() && selectedAction.isNotBlank() && selectedExtra != null && extraValid
    }

    Scaffold(
        topBar = {
            NavBar(title = screenTitle, navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ActionTextInput(
                text = actionText,
                onTextChange = { actionText = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionPicker(
                selectedAction = selectedAction,
                availableActions = availableActions,
                onActionSelected = {
                    selectedAction = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionExtraInput(
                selectedAction = selectedAction,
                selectedExtra = selectedExtra,
                onExtraUpdated = { selectedExtra = it },
                onExtraValidated = { extraValid = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TestButton(
                isEnabled = buttonsEnabled,
                onTestClicked = {
                    if (selectedAction.isNotBlank() && selectedExtra != null) {
                        actionPerformer.test(selectedAction, selectedExtra)
                    }
                }
            )

            SaveButton(
                isEditMode = isEditMode,
                isSaving = isSaving,
                isEnabled = buttonsEnabled,
                onSaveClicked = {
                    isSaving = true
                    if (isEditMode) {
                        actionStore.updateAction(
                            id = actionId,
                            action = selectedAction,
                            text = actionText,
                            extra = selectedExtra
                        )
                    } else {
                        actionStore.addAction(
                            action = selectedAction,
                            text = actionText,
                            extra = selectedExtra
                        )
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun ActionTextInput(
    text: String,
    onTextChange: (String) -> Unit
) {
    TextArea(
        value = text,
        onValueChange = onTextChange,
        label = "Action Text",
        imeAction = ImeAction.Next,
        isError = text.isBlank(),
        supportingText = "Action text is required"
    )
}

@Composable
private fun ActionPicker(
    selectedAction: String,
    availableActions: List<String>,
    onActionSelected: (String) -> Unit
) {
    Picker(
        title = "Select Action",
        selectedItem = selectedAction,
        items = availableActions,
        onItemSelected = onActionSelected,
        itemToString = { it },
        itemDescription = { getActionDescription(it) }
    )
}

@Composable
private fun ActionExtraInput(
    selectedAction: String,
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    when (selectedAction) {
        ACTION_OPEN_APP -> AppLaunchExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )

        ACTION_COPY_TEXT_TO_CLIPBOARD -> CopyTextExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )

        ACTION_CALL_NUMBER -> CallNumberExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )

        ACTION_OPEN_LINK -> OpenLinkExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )

        ACTION_SEND_TEXT -> SendTextExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )

        ACTION_SEND_EMAIL -> SendEmailExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )

        else -> {
            // Handle unknown action types or actions without extras
            onExtraUpdated(null)
            onExtraValidated(true)
        }
    }
}

@Composable
private fun SaveButton(
    isEditMode: Boolean,
    isSaving: Boolean,
    isEnabled: Boolean,
    onSaveClicked: () -> Unit
) {
    FullWidthButton(
        text = if (isEditMode) "Update Action" else "Add Action",
        enabled = isEnabled && !isSaving,
        onClick = onSaveClicked
    )
}

@Composable
private fun TestButton(
    isEnabled: Boolean,
    onTestClicked: () -> Unit
) {
    FullWidthButton(
        text = "Test",
        enabled = isEnabled,
        onClick = onTestClicked
    )
}
