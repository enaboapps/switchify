package com.enaboapps.switchify.screens.settings.switches.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class SwitchesScreenModel(private val store: SwitchEventStore) : ViewModel() {

    val events = MutableLiveData(
        store.getSwitchEvents()
    )

    fun loadEvents() {
        events.postValue(store.getSwitchEvents())
    }

    fun deleteEvent(event: SwitchEvent) {
        store.remove(event)
        loadEvents()
    }

}
