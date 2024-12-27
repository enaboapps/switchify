package com.enaboapps.switchify.switches

/**
 * An enum representing the different facial gestures.
 */
class FacialGesture(val id: String) {
    companion object {
        const val SMILE = "smile"
        const val LEFT_WINK = "left_wink"
        const val RIGHT_WINK = "right_wink"
        const val BLINK = "blink"
    }

    fun getName(): String {
        return when (id) {
            SMILE -> "Smile"
            LEFT_WINK -> "Left Wink"
            RIGHT_WINK -> "Right Wink"
            BLINK -> "Blink"
            else -> "Unknown"
        }
    }

    fun getDescription(): String {
        return when (id) {
            SMILE -> "Smile"
            LEFT_WINK -> "Wink with your left eye"
            RIGHT_WINK -> "Wink with your right eye"
            BLINK -> "Blink with your eyes"
            else -> "Unknown"
        }
    }
}