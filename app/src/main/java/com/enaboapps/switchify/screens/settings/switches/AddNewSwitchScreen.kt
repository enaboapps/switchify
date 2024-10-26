package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.switches.actions.SwitchActionPicker
import com.enaboapps.switchify.screens.settings.switches.models.AddNewSwitchScreenModel
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun AddNewSwitchScreen(navController: NavController) {
    val context = LocalContext.current
    val switchEventStore = SwitchEventStore(context)
    val addNewSwitchScreenModel = remember {
        AddNewSwitchScreenModel(switchEventStore)
    }
    val verticalScrollState = rememberScrollState()
    val shouldSave by addNewSwitchScreenModel.shouldSave.observeAsState()
    val isValid by addNewSwitchScreenModel.isValid.observeAsState()
    val allowLongPress by addNewSwitchScreenModel.allowLongPress.observeAsState()
    Scaffold(
        topBar = {
            NavBar(title = "Add New Switch", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(it)
                .padding(all = 16.dp),
        ) {
            SwitchName(name = addNewSwitchScreenModel.name)
            if (!shouldSave!!) {
                SwitchListener(onKeyEvent = { keyEvent: KeyEvent ->
                    addNewSwitchScreenModel.processKeyCode(keyEvent.key, context)
                })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Switch captured", style = MaterialTheme.typography.labelMedium)
                    SwitchActionSection(addNewSwitchScreenModel, allowLongPress!!)
                    FullWidthButton(text = "Save", enabled = isValid!!, onClick = {
                        addNewSwitchScreenModel.save()
                        navController.popBackStack()
                    })
                }
            }
            Text(
                text = "Is your switch not working? " +
                        "If you are using a USB switch, please make sure that you have it plugged in and that it is turned on. " +
                        "If you are using a Bluetooth switch, please make sure that it is paired with your device.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SwitchListener(onKeyEvent: (KeyEvent) -> Unit) {
    val requester = remember { FocusRequester() }
    Row(modifier = Modifier
        .padding(16.dp)
        .onKeyEvent { keyEvent ->
            onKeyEvent(keyEvent)
            true
        }
        .fillMaxWidth()
        .focusRequester(requester)
        .focusable(),
        horizontalArrangement = Arrangement.Center) {
        Text(text = "Activate your switch", style = MaterialTheme.typography.titleMedium)
    }
    LaunchedEffect(requester) {
        requester.requestFocus()
    }
}

@Composable
fun SwitchName(name: MutableLiveData<String>) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = name.value!!, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SwitchActionSection(
    viewModel: AddNewSwitchScreenModel,
    allowLongPress: Boolean
) {
    val observeLongPressActions = viewModel.longPressActions.observeAsState()
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

        if (allowLongPress) {
            Text(
                text = "Each switch can have multiple actions for long press. " +
                        "You can add or remove actions below. " +
                        "The actions will be executed in the order they are listed based on the duration of the long press.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            observeLongPressActions.value?.forEach { action ->
                SwitchActionPicker(
                    title = "Long Press Action ${observeLongPressActions.value!!.indexOf(action) + 1}",
                    switchAction = action,
                    onChange = { newAction ->
                        viewModel.updateLongPressAction(action, newAction)
                    },
                    onDelete = {
                        viewModel.removeLongPressAction(action)
                    }
                )
            }
            FullWidthButton(text = "Add Long Press Action", onClick = {
                viewModel.addLongPressAction(SwitchAction(SwitchAction.ACTION_SELECT))
            })
        }
    }
}