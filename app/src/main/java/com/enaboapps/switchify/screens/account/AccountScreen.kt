package com.enaboapps.switchify.screens.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.FullWidthButton
import com.enaboapps.switchify.nav.NavigationRoute
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountScreen(navController: NavController) {
    val authManager = AuthManager.instance
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "Not Logged In"

    BaseView(
        title = "Account",
        navController = navController
    ) {
        EmailAddressView(email = userEmail)
        Spacer(modifier = Modifier.height(20.dp))

        FullWidthButton(
            text = "Change Password",
            onClick = {
                navController.navigate(NavigationRoute.ChangePassword.name)
            }
        )

        FullWidthButton(
            text = "Sign Out",
            onClick = {
                authManager.signOut()
                navController.popBackStack(navController.graph.startDestinationId, false)
            }
        )
    }
}

/**
 * This composable represents the email address view
 * @param email The email address
 */
@Composable
private fun EmailAddressView(email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Email address",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            Row {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    modifier = Modifier.padding(8.dp)
                )
                Text(text = email, modifier = Modifier.padding(8.dp))
            }
        }
    }
}