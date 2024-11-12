package com.enaboapps.switchify.screens.account

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.auth.rememberFirebaseAuthLauncher
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.TextArea
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authManager = AuthManager.instance
    val verticalScrollState = rememberScrollState()
    val context = LocalContext.current

    val onSignUp = {
        // Go to the first screen
        navController.popBackStack(
            navController.graph.startDestinationId,
            false
        )

        // Upload the user's settings to Firestore
        val preferenceManager = PreferenceManager(context)
        preferenceManager.preferenceSync.uploadSettingsToFirestore()

        // Start listening for changes to the user's settings
        preferenceManager.preferenceSync.listenForSettingsChangesOnRemote()

        // Push actions to Firestore
        val actionStore = ActionStore(context)
        actionStore.pushActionsToFirestore()
    }

    val authLauncher = rememberFirebaseAuthLauncher(
        onAuthComplete = { authResult ->
            if (authResult.user != null) {
                onSignUp()
            } else {
                errorMessage = "Sign up failed"
            }
        },
        onAuthError = { e ->
            errorMessage = e.message
        }
    )

    Scaffold(topBar = {
        NavBar(
            title = "Sign Up",
            navController = navController
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(verticalScrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(text = "Create an account to save your settings. This will allow you to access your settings on any device.")
            Spacer(modifier = Modifier.height(16.dp))
            TextArea(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = email.isBlank(),
                supportingText = "Email is required"
            )
            Spacer(modifier = Modifier.height(8.dp)) // Add some spacing
            TextArea(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isSecure = true,
                isError = password.isBlank(),
                supportingText = "Password is required"
            )
            Spacer(modifier = Modifier.height(8.dp)) // Add some spacing
            TextArea(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                keyboardType = KeyboardType.Password,
                isSecure = true,
                isError = confirmPassword.isBlank(),
                supportingText = "Confirm password is required"
            )
            Spacer(modifier = Modifier.height(16.dp))
            FullWidthButton(
                text = "Sign Up",
                onClick = {
                    errorMessage = when {
                        !authManager.isPasswordStrong(password) -> "Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, and a number."
                        password != confirmPassword -> "Passwords do not match."
                        email.isEmpty() -> "Email cannot be empty."
                        password.isEmpty() -> "Password cannot be empty."
                        else -> null
                    }
                    if (errorMessage == null) {
                        authManager.createUserWithEmailAndPassword(email, password,
                            onSuccess = {
                                onSignUp()
                            },
                            onFailure = { exception ->
                                errorMessage = exception.localizedMessage
                            }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("or")
            Spacer(modifier = Modifier.height(16.dp))
            val token = stringResource(id = R.string.default_web_client_id)
            FullWidthButton(
                text = "Sign up with Google",
                onClick = {
                    val googleSignIn = GoogleSignIn.getClient(
                        context,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(token)
                            .requestEmail()
                            .build()
                    )
                    authLauncher.launch(googleSignIn.signInIntent)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            val urlLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    // Handle the result
                }
            val privacyPolicyUrl = "https://www.enaboapps.com/switchify-privacy-policy"
            FullWidthButton(
                text = "Privacy Policy",
                onClick = {
                    // Open the privacy policy in the system browser
                    urlLauncher.launch(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)))
                },
                isTextButton = true
            )
        }
    }
}