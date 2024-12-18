package com.enaboapps.switchify.screens.settings.lockscreen.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enaboapps.switchify.backend.preferences.PreferenceManager
import kotlinx.coroutines.launch

class LockScreenSettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    private val _lockScreen = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN)
    }
    val lockScreen: LiveData<Boolean> = _lockScreen

    private val _lockScreenCode = MutableLiveData<String>().apply {
        value = preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN_CODE)
    }
    val lockScreenCode: LiveData<String> = _lockScreenCode

    fun setLockScreen(lockScreenEnabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN,
                lockScreenEnabled
            )
            _lockScreen.postValue(lockScreenEnabled)
        }
    }

    fun setLockScreenCode(lockScreenCode: String) {
        viewModelScope.launch {
            preferenceManager.setStringValue(
                PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN_CODE,
                lockScreenCode
            )
            _lockScreenCode.postValue(lockScreenCode)
        }
    }
}