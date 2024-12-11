package com.enaboapps.switchify.screens.settings.prediction

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.thingthing.fleksy.lib.languages.RemoteLanguage
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.components.UICard
import com.enaboapps.switchify.keyboard.prediction.PredictionLanguageManager

@Composable
fun PredictionLanguageScreen(navController: NavController) {
    val context = LocalContext.current
    val manager = remember { PredictionLanguageManager(context) }
    var languages by remember { mutableStateOf(emptyList<RemoteLanguage?>()) }

    val loadLanguages = {
        manager.getAvailableLanguages { available ->
            languages = available
        }
    }

    LaunchedEffect(Unit) {
        loadLanguages()
    }

    BaseView(
        title = "Choose Prediction Language",
        navController = navController
    ) {
        // Display available languages
        languages.forEach {
            if (it != null) {
                LanguageRow(it, manager) {
                    languages = emptyList()
                    loadLanguages()
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    language: RemoteLanguage,
    predictionLanguageManager: PredictionLanguageManager,
    onDownloaded: () -> Unit
) {
    var downloaded by remember { mutableStateOf(false) }
    downloaded = predictionLanguageManager.getCurrentLanguageFilePath().contains(language.locale)
    val context = LocalContext.current
    Column {
        Spacer(modifier = Modifier.padding(8.dp))
        UICard(
            title = language.locale,
            description = if (downloaded) "Downloaded" else "Not downloaded",
            onClick = {
                // Download language
                if (!downloaded) {
                    predictionLanguageManager.downloadLanguage(language) { success ->
                        if (success) {
                            onDownloaded()
                            Toast.makeText(context, "Language downloaded", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to download language",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            })
    }
}