package com.enaboapps.switchify.screens.settings.switches.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.switches.SwitchEventStore.RemoteSwitchInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Switches screen, handling switch events and remote switch operations.
 */
class SwitchesScreenModel(private val store: SwitchEventStore) : ViewModel() {

    private val _uiState = MutableStateFlow(SwitchesUiState())
    val uiState: StateFlow<SwitchesUiState> = _uiState

    init {
        loadEvents()
    }

    /**
     * Loads local switch events and fetches remote switches.
     */
    fun loadEvents() {
        viewModelScope.launch {
            // Update local switches immediately
            updateLocalSwitches()

            // Start loading remote switches
            _uiState.value = _uiState.value.copy(isLoading = true)

            store.fetchAvailableSwitches()
                .onSuccess { remoteSwitches ->
                    _uiState.value = _uiState.value.copy(
                        remoteSwitches = remoteSwitches,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
        }
    }

    /**
     * Imports a single remote switch.
     */
    fun importSwitch(remoteSwitch: RemoteSwitchInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                importingSwitch = remoteSwitch.code
            )

            store.importSwitch(remoteSwitch.code)
                .onSuccess {
                    updateLocalSwitches()
                    // Refresh remote switches to update isOnDevice status
                    loadEvents()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        importingSwitch = null
                    )
                }
        }
    }

    /**
     * Deletes a remote switch.
     */
    fun deleteRemoteSwitch(remoteSwitch: RemoteSwitchInfo) {
        viewModelScope.launch {
            store.removeRemote(remoteSwitch.code)
                .onSuccess {
                    updateLocalSwitches()
                    loadEvents()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        importingSwitch = null
                    )
                }
        }
    }

    /**
     * Updates the local switches in the UI state.
     */
    private fun updateLocalSwitches() {
        _uiState.value = _uiState.value.copy(
            localSwitches = store.getSwitchEvents()
        )
    }
}

/**
 * Represents the UI state for the Switches screen.
 */
data class SwitchesUiState(
    val localSwitches: Set<SwitchEvent> = emptySet(),
    val remoteSwitches: List<RemoteSwitchInfo> = emptyList(),
    val isLoading: Boolean = false,
    val importingSwitch: String? = null
)