package com.enaboapps.switchify.screens.account

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.FullWidthButton
import com.enaboapps.switchify.components.TextArea

@Composable
fun ChangePasswordScreen(navController: NavController) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val authManager = AuthManager.instance

    BaseView(
        title = "Change Password",
        navController = navController
    ) {
        if (message != null) {
            Text(
                text = message ?: "",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextArea(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = "Current Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            isSecure = true,
            isError = currentPassword.isBlank(),
            supportingText = "Current password is required"
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextArea(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "New Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            isSecure = true,
            isError = newPassword.isBlank(),
            supportingText = "New password is required"
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextArea(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm New Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isSecure = true,
            isError = confirmPassword.isBlank(),
            supportingText = "Confirm new password is required"
        )
        Spacer(modifier = Modifier.height(16.dp))
        FullWidthButton(
            text = "Change Password",
            onClick = {
                message = when {
                    newPassword != confirmPassword -> "New passwords do not match."
                    !authManager.isPasswordStrong(newPassword) -> "New password is not strong enough."
                    else -> null
                }
                if (message == null) {
                    authManager.updatePassword(currentPassword, newPassword,
                        onSuccess = {
                            message = "Password changed successfully."
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            message = exception.localizedMessage ?: "An error occurred."
                        }
                    )
                }
            }
        )
    }
}