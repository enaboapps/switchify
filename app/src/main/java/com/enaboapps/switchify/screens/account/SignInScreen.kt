package com.enaboapps.switchify.screens.account

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.auth.GoogleAuthHandler
import com.enaboapps.switchify.components.*
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthHandler = remember { GoogleAuthHandler() }

    val onSignIn = {
        // Download user settings from Firestore
        val preferenceManager = PreferenceManager(context)
        preferenceManager.preferenceSync.retrieveSettingsFromFirestore()

        // Listen for changes to user settings
        preferenceManager.preferenceSync.listenForSettingsChangesOnRemote()

        // Pull actions from Firestore
        val actionStore = ActionStore(context)
        actionStore.pullActionsFromFirestore()
    }

    BaseView(
        title = "Sign In",
        navController = navController
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(text = "Sign in to access your settings.")
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

        Spacer(modifier = Modifier.height(8.dp))

        TextArea(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            isSecure = true,
            isError = password.isBlank(),
            supportingText = "Password is required"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FullWidthButton(
            text = "Sign In",
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    AuthManager.instance.signInWithEmailAndPassword(
                        email, password,
                        onSuccess = {
                            navController.popBackStack()
                            onSignIn()
                        },
                        onFailure = { exception ->
                            errorMessage = exception.localizedMessage
                        }
                    )
                } else {
                    errorMessage = "Please enter your email and password"
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        FullWidthButton(
            text = "Sign Up",
            onClick = {
                navController.navigate(NavigationRoute.SignUp.name)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("or")
        Spacer(modifier = Modifier.height(16.dp))

        FullWidthButton(
            text = "Sign in with Google",
            onClick = {
                scope.launch {
                    googleAuthHandler.googleSignIn(context).collect { result ->
                        result.fold(
                            onSuccess = { authResult ->
                                if (authResult.user != null) {
                                    navController.popBackStack()
                                    onSignIn()
                                } else {
                                    errorMessage = "Sign in failed"
                                }
                            },
                            onFailure = { exception ->
                                errorMessage = exception.message
                            }
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FullWidthButton(
            text = "Forgot Password?",
            onClick = {
                navController.navigate(NavigationRoute.ForgotPassword.name)
            },
            isTextButton = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        val urlLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            // Handle the result
        }

        val privacyPolicyUrl = "https://www.switchifyapp.com/privacy"

        FullWidthButton(
            text = "Privacy Policy",
            onClick = {
                urlLauncher.launch(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)))
            },
            isTextButton = true
        )
    }
}