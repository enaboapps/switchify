package com.enaboapps.switchify.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.enaboapps.switchify.activities.ui.theme.SwitchifyTheme
import com.enaboapps.switchify.backend.iap.IAPHandler
import com.enaboapps.switchify.nav.NavGraph
import com.enaboapps.switchify.backend.preferences.PreferenceManager
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.utils.Logger

class MainActivity : ComponentActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var actionStore: ActionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeManagers()

        Logger.log(this, "Launched Switchify")

        setContent {
            val navController = rememberNavController()

            SwitchifyTheme {
                NavGraph(navController = navController)
            }
        }
    }

    private fun initializeManagers() {
        // Initialize PreferenceManager
        preferenceManager = PreferenceManager(this)
        preferenceManager.preferenceSync.apply {
            retrieveSettingsFromFirestore()
            listenForSettingsChangesOnRemote()
        }

        // Initialize ActionStore
        actionStore = ActionStore(this)
        actionStore.pullActionsFromFirestore()

        // Initialize IAP
        IAPHandler.initialize(this)
    }
}
