package com.enaboapps.switchify.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {
    companion object {
        val instance: AuthManager by lazy {
            AuthManager()
        }
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Create user with email and password.
     */
    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception!!)
                }
            }
    }

    /**
     * Sign in with email and password.
     */
    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception!!)
                }
            }
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Check if a user is currently signed in.
     */
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get the currently signed-in user, if any.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Get the user's ID, if any.
     */
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Update the current user's password.
     */
    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user!!.email!!, currentPassword)

        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(updateTask.exception ?: Exception("Failed to update password"))
                    }
                }
            } else {
                onFailure(authTask.exception ?: Exception("Re-authentication failed"))
            }
        }
    }

    /**
     * Send a password reset email.
     */
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception ?: Exception("Unknown error occurred"))
                }
            }
    }

    /**
     * Check a password for strength.
     */
    fun isPasswordStrong(password: String): Boolean {
        // eight characters, one uppercase, one lowercase, one number
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}\$".toRegex()
        return passwordRegex.matches(password)
    }
}