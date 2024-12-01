package com.enaboapps.switchify.screens.settings.lockscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.lockscreen.models.LockScreenSettingsScreenModel
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.PreferenceSwitch
import com.enaboapps.switchify.widgets.Section
import com.enaboapps.switchify.widgets.TextArea

@Composable
fun LockScreenSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val lockScreenSettingsScreenModel = LockScreenSettingsScreenModel(context)
    val verticalScrollState = rememberScrollState()
    val currentLockScreenCode = MutableLiveData<String>()
    currentLockScreenCode.value = lockScreenSettingsScreenModel.lockScreenCode.value
    val currentLockScreen = MutableLiveData<Boolean>()
    currentLockScreen.value = lockScreenSettingsScreenModel.lockScreen.value
    val observeLockScreen = lockScreenSettingsScreenModel.lockScreen.observeAsState()
    val setLockScreen = { lockScreenEnabled: Boolean ->
        lockScreenSettingsScreenModel.setLockScreen(lockScreenEnabled)
        currentLockScreen.value = lockScreenEnabled
    }
    val setLockScreenCode = { lockScreenCode: String ->
        lockScreenSettingsScreenModel.setLockScreenCode(lockScreenCode)
        currentLockScreenCode.value = lockScreenCode
    }
    Scaffold(
        topBar = {
            NavBar("Lock Screen Settings", navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp)
                .verticalScroll(verticalScrollState),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Why would I want to enable the lock screen?",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The default Android lock screen can sometimes disable Accessibility Services, such as Switchify. Our lock screen is designed to always work with Switchify. " +
                        "We recommend disabling the default lock screen and using our lock screen instead." +
                        "\nDisclaimer: The default lock screen is built with high security standards. We cannot guarantee that our lock screen will not be hacked or compromised.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceSwitch(
                title = "Lock Screen",
                summary = "Enable the lock screen",
                checked = observeLockScreen.value == true,
                onCheckedChange = {
                    setLockScreen(it)
                }
            )
            if (observeLockScreen.value == true) {
                LockScreenCodeInput(
                    lockScreenCode = currentLockScreenCode.value ?: "",
                    onLockScreenCodeChanged = {
                        setLockScreenCode(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun LockScreenCodeInput(
    lockScreenCode: String,
    onLockScreenCodeChanged: (String) -> Unit
) {
    var code by remember { mutableStateOf(lockScreenCode) }
    var confirmCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf("") }
    var confirmError by remember { mutableStateOf("") }
    var loadedCode: String? by remember { mutableStateOf(null) }
    var confirmPreviousCode by remember { mutableStateOf("") }
    var previousCodeError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (code.isNotEmpty()) {
            loadedCode = code
        }
    }

    var isSettingLockScreenCode by remember { mutableStateOf(false) }

    Section(title = "Lock Screen Code") {
        if (isSettingLockScreenCode) {
            if (loadedCode != null && confirmPreviousCode != loadedCode) {
                TextArea(
                    value = confirmPreviousCode,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            confirmPreviousCode = newValue
                            previousCodeError = if (confirmPreviousCode != loadedCode) {
                                "Incorrect previous code"
                            } else ""
                        }
                    },
                    label = "Confirm Previous Lock Screen Code",
                    keyboardType = KeyboardType.Number,
                    isSecure = true,
                    isError = previousCodeError.isNotEmpty(),
                    supportingText = previousCodeError.ifEmpty { null }
                )
            } else {
                TextArea(
                    value = code,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            code = newValue
                            codeError = when {
                                code.isEmpty() -> "Code cannot be empty"
                                code.length != 4 -> "Code must be 4 digits"
                                code != confirmCode && confirmCode.isNotEmpty() -> "Codes do not match"
                                else -> ""
                            }
                        }
                    },
                    label = "Lock Screen Code",
                    keyboardType = KeyboardType.Number,
                    isSecure = true,
                    isError = codeError.isNotEmpty(),
                    supportingText = codeError.ifEmpty { null }
                )

                TextArea(
                    value = confirmCode,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            confirmCode = newValue
                            confirmError = when {
                                code != confirmCode -> "Codes do not match"
                                else -> ""
                            }
                            // Also update code error if codes don't match
                            if (code.isNotEmpty()) {
                                codeError = if (code != confirmCode) "Codes do not match" else ""
                            }
                        }
                    },
                    label = "Confirm Lock Screen Code",
                    keyboardType = KeyboardType.Number,
                    isSecure = true,
                    isError = confirmError.isNotEmpty(),
                    supportingText = confirmError.ifEmpty { null }
                )

                if (loadedCode != null && confirmPreviousCode == loadedCode) {
                    FullWidthButton(
                        text = "Don't Use a Code",
                        onClick = {
                            onLockScreenCodeChanged("")
                            isSettingLockScreenCode = false
                        }
                    )
                }

                FullWidthButton(
                    text = "Save",
                    enabled = code.length == 4 && code == confirmCode,
                    onClick = {
                        if (code.length == 4 && code == confirmCode) {
                            onLockScreenCodeChanged(code)
                            isSettingLockScreenCode = false
                        }
                    }
                )
            }
        } else {
            FullWidthButton(
                text = if (code.isNotEmpty()) "Change Code" else "Set Code",
                onClick = {
                    isSettingLockScreenCode = true
                    codeError = ""
                    confirmError = ""
                    previousCodeError = ""
                    code = ""
                    confirmCode = ""
                }
            )
        }
    }
}