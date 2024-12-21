package com.enaboapps.switchify.utils

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.enaboapps.switchify.BuildConfig

/**
 * Logger class for logging events and messages to Amplitude.
 */
object Logger {
    private lateinit var amplitude: Amplitude

    /**
     * Initializes the logger with the given context.
     *
     * @param context The context to initialize the logger with.
     */
    fun init(context: Context) {
        val apiKey = BuildConfig.AMPLITUDE_API_KEY
        amplitude = Amplitude(Configuration(apiKey, context.applicationContext))
    }

    /**
     * Logs an event with the given name.
     *
     * @param eventName The name of the event to log.
     */
    fun logEvent(eventName: String) {
        amplitude.track(eventName)
        println("Event logged: $eventName")
    }
}