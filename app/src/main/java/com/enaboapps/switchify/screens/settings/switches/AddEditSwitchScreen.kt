package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.switches.actions.SwitchActionPicker
import com.enaboapps.switchify.screens.settings.switches.models.AddEditSwitchScreenModel
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.TextArea

@Composable
fun AddEditSwitchScreen(navController: NavController, code: String? = null) {
    val context = LocalContext.current
    val switchEventStore = SwitchEventStore(context)
    val addEditSwitchScreenModel = remember {
        AddEditSwitchScreenModel().apply {
            init(code, switchEventStore, context)
        }
    }
    val verticalScrollState = rememberScrollState()
    val shouldSave by addEditSwitchScreenModel.shouldSave.observeAsState()
    val isValid by addEditSwitchScreenModel.isValid.observeAsState()
    val editing = code != null
    val captured by addEditSwitchScreenModel.switchCaptured.observeAsState()
    val screenTitle = if (editing) "Edit Switch" else "Add New Switch"
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NavBar(title = screenTitle, navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(it)
                .padding(all = 16.dp),
        ) {
            SwitchName(name = addEditSwitchScreenModel.name, onNameChange = {
                addEditSwitchScreenModel.updateName(it)
            })
            if (!captured!!) {
                SwitchListener(onKeyEvent = { keyEvent: KeyEvent ->
                    addEditSwitchScreenModel.processKeyCode(keyEvent.key, context)
                })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SwitchActionSection(addEditSwitchScreenModel)
                    if (shouldSave!!) {
                        FullWidthButton(text = "Save", enabled = isValid!!, onClick = {
                            addEditSwitchScreenModel.save()
                            navController.popBackStack()
                        })
                    }
                    if (editing) {
                        FullWidthButton(text = "Delete", onClick = {
                            showDeleteConfirmation.value = true
                        })
                    }
                }

                if (showDeleteConfirmation.value) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmation.value = false },
                        title = { Text("Confirm Deletion") },
                        text = { Text("Are you sure you want to delete this switch?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmation.value = false
                                    addEditSwitchScreenModel.delete {
                                        navController.popBackStack()
                                    }
                                }
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteConfirmation.value = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchListener(onKeyEvent: (KeyEvent) -> Unit) {
    val requester = remember { FocusRequester() }
    Column(modifier = Modifier
        .padding(16.dp)
        .onKeyEvent { keyEvent ->
            onKeyEvent(keyEvent)
            true
        }
        .fillMaxWidth()
        .focusRequester(requester)
        .focusable(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Activate your switch", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Is your switch not working? " +
                    "If you are using a USB switch, please make sure that you have it plugged in and that it is turned on. " +
                    "If you are using a Bluetooth switch, please make sure that it is paired with your device.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
    LaunchedEffect(requester) {
        requester.requestFocus()
    }
}

@Composable
fun SwitchName(
    name: String = "",
    onNameChange: (String) -> Unit
) {
    var name by remember { mutableStateOf(name) }

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TextArea(
            value = name,
            onValueChange = {
                name = it
                onNameChange(it)
            },
            label = "Switch Name",
            isError = name.isBlank(),
            supportingText = "Switch name is required"
        )
    }
}

@Composable
fun SwitchActionSection(viewModel: AddEditSwitchScreenModel) {
    val allowLongPress = viewModel.allowLongPress.observeAsState()
    val longPressActions = viewModel.longPressActions.observeAsState()
    val refreshingLongPressActions = viewModel.refreshingLongPressActions.observeAsState()
    val context = LocalContext.current
    Column {
        SwitchActionPicker(
            title = "Press Action",
            switchAction = viewModel.pressAction.value!!,
            onChange = {
                viewModel.setPressAction(it, context)
            }
        )

        Spacer(modifier = Modifier.padding(16.dp))

        if (allowLongPress.value!! && !refreshingLongPressActions.value!!) {
            Text(
                text = "Each switch can have multiple actions for long press. " +
                        "You can add or remove actions below. " +
                        "The actions will be executed in the order they are listed based on the duration of the long press.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            longPressActions.value?.forEachIndexed { index, action ->
                SwitchActionPicker(
                    title = "Long Press Action ${index + 1}",
                    switchAction = action,
                    onChange = { newAction ->
                        viewModel.updateLongPressAction(action, newAction)
                    },
                    onDelete = {
                        viewModel.removeLongPressAction(index)
                    }
                )
            }
            FullWidthButton(text = "Add Long Press Action", onClick = {
                viewModel.addLongPressAction(SwitchAction(SwitchAction.ACTION_SELECT))
            })
        }
    }
}
