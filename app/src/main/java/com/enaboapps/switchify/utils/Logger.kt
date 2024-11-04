package com.enaboapps.switchify.utils

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class Logger {
    companion object {
        fun log(context: Context, message: String) {
            FirebaseAnalytics.getInstance(context).logEvent(message, null)
            println(message)
        }
    }
}