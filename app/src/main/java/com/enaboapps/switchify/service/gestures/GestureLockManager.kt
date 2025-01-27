package com.enaboapps.switchify.service.gestures

import com.enaboapps.switchify.backend.iap.IAPHandler
import com.enaboapps.switchify.service.gestures.data.GestureData
import com.enaboapps.switchify.service.gestures.data.GestureType
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.window.ServiceMessageHUD

class GestureLockManager {
    private var isLocked = false
    private var lockedGestureData: GestureData? = null

    // Function to lock/unlock the gesture lock, showing a message to the user
    fun toggleGestureLock() {
        if (!IAPHandler.hasPurchasedPro()) {
            ServiceMessageHUD.instance.showMessage(
                "Gesture lock is a Pro feature. Please upgrade to Pro to use this feature.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
            return
        }

        isLocked = !isLocked
        if (isLocked) {
            ServiceMessageHUD.instance.showMessage(
                "Gesture lock enabled. Choose a gesture to lock to your switch. You can disable it by holding your switch.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
        } else {
            ServiceMessageHUD.instance.showMessage(
                "Gesture lock disabled. Your switches will now perform their default actions.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )

            // Clear the locked gesture data
            lockedGestureData = null
        }
    }

    // Function to check if the gesture lock is enabled and the user is not in the menu
    // and the locked gesture data is not null
    fun isGestureLockEnabled(): Boolean {
        return isLocked && !ScanMethod.isInMenu && lockedGestureData != null
    }

    // Function to get the locked gesture data
    fun getLockedGestureData(): GestureData? {
        return lockedGestureData
    }

    // Function to set the locked gesture data
    fun setLockedGestureData(gestureData: GestureData?) {
        lockedGestureData =
            if (gestureData != null && canLockGesture(gestureData.gestureType) && isLocked) {
                gestureData
            } else {
                null
            }
    }

    // Function to check if a gesture type can be locked
    fun canLockGesture(gestureType: GestureType): Boolean {
        if (isLocked && gestureType == GestureType.DRAG || gestureType == GestureType.CUSTOM_SWIPE) {
            isLocked = false // Disable the gesture lock
            setLockedGestureData(null) // Clear the locked gesture data
            return false
        }
        return true
    }

    // Function to inform the user that the type of gesture cannot be locked
    fun informCannotLockGesture(type: GestureType) {
        if (!canLockGesture(type)) {
            ServiceMessageHUD.instance.showMessage(
                "Cannot lock drag or custom swipe gestures. Gesture lock disabled.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
        }
    }
}
