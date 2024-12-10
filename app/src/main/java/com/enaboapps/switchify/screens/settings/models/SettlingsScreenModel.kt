package com.enaboapps.switchify.screens.settings.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enaboapps.switchify.preferences.PreferenceManager
import kotlinx.coroutines.launch

class SettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    private val _autoSelect = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }
    val autoSelect: LiveData<Boolean> = _autoSelect

    private val _autoSelectDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
    }
    val autoSelectDelay: LiveData<Long> = _autoSelectDelay

    private val _assistedSelection = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_ASSISTED_SELECTION)
    }
    val assistedSelection: LiveData<Boolean> = _assistedSelection

    private val _menuTransparency = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_MENU_TRANSPARENCY)
    }
    val menuTransparency: LiveData<Boolean> = _menuTransparency

    fun setAutoSelect(autoSelect: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT,
                autoSelect
            )
            _autoSelect.postValue(autoSelect)
        }
    }

    fun setAutoSelectDelay(delay: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY,
                delay
            )
            _autoSelectDelay.postValue(delay)
        }
    }

    fun setAssistedSelection(assistedSelection: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_ASSISTED_SELECTION,
                assistedSelection
            )
            _assistedSelection.postValue(assistedSelection)
        }
    }

    fun setMenuTransparency(value: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_MENU_TRANSPARENCY,
                value
            )
            _menuTransparency.postValue(value)
        }
    }
}