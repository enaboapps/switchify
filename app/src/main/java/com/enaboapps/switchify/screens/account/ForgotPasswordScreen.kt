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
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val authManager = AuthManager.instance

    BaseView(
        title = "Reset Password",
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
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            isError = email.isBlank(),
            supportingText = "Email is required"
        )
        Spacer(modifier = Modifier.height(16.dp))
        FullWidthButton(
            text = "Send Reset Link",
            onClick = {
                if (email.isNotBlank()) {
                    authManager.sendPasswordResetEmail(email,
                        onSuccess = {
                            message = "Reset link sent to your email. Please check your inbox."
                        },
                        onFailure = { exception ->
                            message = "Error: ${exception.localizedMessage}. Please try again."
                        }
                    )
                } else {
                    message = "Please enter your email address."
                }
            }
        )
    }
}