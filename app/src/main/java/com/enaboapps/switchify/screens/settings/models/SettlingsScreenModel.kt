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

    // Initialize MutableLiveData with initial values from PreferenceManager
    private val _scanRate = MutableLiveData<Long>().apply {
        value = preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }
    val scanRate: LiveData<Long> = _scanRate
    private val _radarScanRate = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_RADAR_SCAN_RATE)
    }
    val radarScanRate: LiveData<Long> = _radarScanRate

    private val _pauseOnFirstItem = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM)
    }
    val pauseOnFirstItem: LiveData<Boolean> = _pauseOnFirstItem

    private val _pauseOnFirstItemDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY)
    }
    val pauseOnFirstItemDelay: LiveData<Long> = _pauseOnFirstItemDelay

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

    private val _rowColumnScan = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN)
    }
    val rowColumnScan: LiveData<Boolean> = _rowColumnScan

    private val _groupScan = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_GROUP_SCAN)
    }
    val groupScan: LiveData<Boolean> = _groupScan

    private val _automaticallyStartScanAfterSelection = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTOMATICALLY_START_SCAN_AFTER_SELECTION
        )
    }
    val automaticallyStartScanAfterSelection: LiveData<Boolean> =
        _automaticallyStartScanAfterSelection

    private val _moveRepeat = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_MOVE_REPEAT)
    }
    val moveRepeat: LiveData<Boolean> = _moveRepeat

    private val _moveRepeatDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_MOVE_REPEAT_DELAY)
    }
    val moveRepeatDelay: LiveData<Long> = _moveRepeatDelay

    private val _menuTransparency = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_MENU_TRANSPARENCY)
    }
    val menuTransparency: LiveData<Boolean> = _menuTransparency


    // Update methods now update MutableLiveData which in turn updates the UI
    fun setScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
            _scanRate.postValue(rate)
        }
    }

    fun setRadarScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_RADAR_SCAN_RATE,
                rate
            )
        }
    }

    fun setPauseOnFirstItem(pause: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM,
                pause
            )
            _pauseOnFirstItem.postValue(pause)
        }
    }

    fun setPauseOnFirstItemDelay(delay: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY,
                delay
            )
            _pauseOnFirstItemDelay.postValue(delay)
        }
    }

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

    fun setAutomaticallyStartScanAfterSelection(automaticallyStartScanAfterSelection: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTOMATICALLY_START_SCAN_AFTER_SELECTION,
                automaticallyStartScanAfterSelection
            )
            _automaticallyStartScanAfterSelection.postValue(automaticallyStartScanAfterSelection)
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

    fun setRowColumnScan(rowColumnScan: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN,
                rowColumnScan
            )
            _rowColumnScan.postValue(rowColumnScan)
        }
    }

    fun setGroupScan(groupScan: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_GROUP_SCAN,
                groupScan
            )
            _groupScan.postValue(groupScan)
        }
    }

    fun setMoveRepeat(moveRepeat: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_MOVE_REPEAT,
                moveRepeat
            )
            _moveRepeat.postValue(moveRepeat)
        }
    }

    fun setMoveRepeatDelay(delay: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_MOVE_REPEAT_DELAY,
                delay
            )
            _moveRepeatDelay.postValue(delay)
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