package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import co.thingthing.fleksy.lib.api.FleksyLib
import co.thingthing.fleksy.lib.api.LibraryConfiguration
import co.thingthing.fleksy.lib.model.TypingContext
import com.enaboapps.switchify.BuildConfig
import com.enaboapps.switchify.keyboard.utils.TextParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface PredictionListener {
    fun onPredictionsAvailable(predictions: List<String>)
}

class PredictionManager(private val context: Context, private val listener: PredictionListener) :
    CoroutineScope {

    private lateinit var fleksyLib: FleksyLib

    private val predictionLanguageManager = PredictionLanguageManager(context)

    private val predictionJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + predictionJob

    fun initialize() {
        val apiKey = BuildConfig.FLEKSY_API_KEY
        val secret = BuildConfig.FLEKSY_API_SECRET
        println("API Key: $apiKey")
        println("Secret: $secret")
        val licence = LibraryConfiguration.LicenseConfiguration(apiKey, secret)
        val languageFile = predictionLanguageManager.getFleksyLanguage()
        val config = LibraryConfiguration(licence)
        fleksyLib = FleksyLib(context.applicationContext, languageFile, config)
    }

    fun destroy() {
        fleksyLib.destroy()
    }

    fun reloadLanguage() {
        val languageFile = predictionLanguageManager.getFleksyLanguage()
        fleksyLib.reloadLanguageFile(languageFile)
    }

    fun predict(textParser: TextParser) {
        // If the latest word has a number, don't get predictions
        if (textParser.latestWordHasNumber()) {
            listener.onPredictionsAvailable(emptyList())
            return
        }

        // Get the current paragraph of the text to predict next words
        val text = textParser.getLatestParagraph()

        // If the last character is a space, get predictions for the next word
        if (text.isNotEmpty() && text.last() == ' ') {
            getPredictionsForNextWord(text)
            learn(text)
        } else if (text.isNotEmpty()) {
            getCurrentPredictions(text)
        } else {
            getCurrentPredictions(" ")
        }
    }

    private fun getCurrentPredictions(text: String) {
        println("Getting predictions for: $text")
        val typingContext = TypingContext(text)
        launch {
            val result = fleksyLib.currentWordPrediction(typingContext)
            if (result.isSuccess) {
                val predictions = result.getOrNull()
                if (predictions != null) {
                    // Prevent duplicate predictions
                    val uniquePredictions =
                        predictions.distinctBy { it.label }.sortedByDescending { it.score }
                    println("Unique predictions: ${uniquePredictions.map { it.label }}")
                    listener.onPredictionsAvailable(uniquePredictions.map { it.label })
                }
            } else {
                println("Error: ${result.exceptionOrNull()}")
            }
        }
    }

    private fun getPredictionsForNextWord(text: String) {
        println("Getting predictions for next word after: $text")
        val typingContext = TypingContext(text)
        launch {
            val result = fleksyLib.nextWordPrediction(typingContext)
            if (result.isSuccess) {
                val predictions = result.getOrNull()
                if (predictions != null) {
                    // Prevent duplicate predictions
                    val uniquePredictions =
                        predictions.distinctBy { it.label }.sortedByDescending { it.score }
                    println("Unique predictions: ${uniquePredictions.map { it.label }}")
                    listener.onPredictionsAvailable(uniquePredictions.map { it.label })
                }
            } else {
                println("Error: ${result.exceptionOrNull()}")
            }
        }
    }

    private fun learn(text: String) {
        launch {
            fleksyLib.addPhrasesToDictionary(List(1) { text })
        }
    }
}